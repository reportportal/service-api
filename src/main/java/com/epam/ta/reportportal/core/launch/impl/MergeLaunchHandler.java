/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.item.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.item.merge.strategy.MergeStrategy;
import com.epam.ta.reportportal.core.item.merge.strategy.MergeStrategyFactory;
import com.epam.ta.reportportal.core.item.merge.strategy.MergeStrategyType;
import com.epam.ta.reportportal.core.launch.IMergeLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.core.statistics.StatisticsHelper;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectRole;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.database.entity.Status.IN_PROGRESS;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.TO_INVESTIGATE;
import static com.epam.ta.reportportal.database.entity.project.ProjectUtils.findUserConfigByLogin;
import static com.epam.ta.reportportal.database.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

/**
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
@Service
public class MergeLaunchHandler implements IMergeLaunchHandler {

	private TestItemRepository testItemRepository;

	private ProjectRepository projectRepository;

	private LaunchRepository launchRepository;

	private UserRepository userRepository;

	@Autowired
	private MergeStrategyFactory mergeStrategyFactory;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private LaunchMetaInfoRepository launchCounter;

	@Autowired
	private TestItemUniqueIdGenerator identifierGenerator;

	@Autowired
	private ILogIndexer logIndexer;

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public LaunchResource mergeLaunches(String projectName, String userName, MergeLaunchesRQ rq) {
		User user = userRepository.findOne(userName);
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		Set<String> launchesIds = rq.getLaunches();
		expect(launchesIds.size() > 1, equalTo(true)).verify(BAD_REQUEST_ERROR, rq.getLaunches());
		List<Launch> launchesList = launchRepository.find(launchesIds);
		boolean hasRetries = launchesList.stream().anyMatch(it -> it.getHasRetries() != null);
		validateMergingLaunches(launchesList, user, project);

		Launch launch = createResultedLaunch(projectName, userName, rq, hasRetries, launchesList);
		boolean isNameChanged = !launch.getName().equals(launchesList.get(0).getName());
		updateChildrenOfLaunches(launch.getId(), rq.getLaunches(), rq.isExtendSuitesDescription(), isNameChanged);

		MergeStrategyType type = MergeStrategyType.fromValue(rq.getMergeStrategyType());
		expect(type, notNull()).verify(UNSUPPORTED_MERGE_STRATEGY_TYPE, type);

		// deep merge strategies
		if (!type.equals(MergeStrategyType.BASIC)) {
			MergeStrategy strategy = mergeStrategyFactory.getStrategy(type);
			//  group items by unique id
			testItemRepository.findWithoutParentByLaunchRef(launch.getId())
					.stream()
					.collect(groupingBy(TestItem::getUniqueId))
					.entrySet()
					.stream()
					.map(Map.Entry::getValue)
					.filter(items -> items.size() > 1)
					.forEach(items -> strategy.mergeTestItems(items.get(0), items.subList(1, items.size())));
		}

		StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(
				project.getConfiguration().getStatisticsCalculationStrategy());
		statisticsFacade.recalculateStatistics(launch);

		launch = launchRepository.findOne(launch.getId());
		launch.setStatus(StatisticsHelper.getStatusFromStatistics(launch.getStatistics()));

		launch.setEndTime(ofNullable(rq.getEndTime()).orElse(launchesList.stream()
				.sorted(comparing(Launch::getEndTime).reversed())
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
				.getEndTime()));

		launchRepository.save(launch);
		launchRepository.delete(launchesIds);

		logIndexer.indexLogs(launch.getId(), testItemRepository.findItemsNotInIssueType(TO_INVESTIGATE.getLocator(), launch.getId()));

		return LaunchConverter.TO_RESOURCE.apply(launch);
	}

	/**
	 * Validations for merge launches request parameters and data
	 *
	 * @param launches
	 */
	private void validateMergingLaunches(List<Launch> launches, User user, Project project) {
		expect(launches.size(), not(equalTo(0))).verify(BAD_REQUEST_ERROR, launches);

		/*
		 * ADMINISTRATOR and PROJECT_MANAGER+ users have permission to merge not-only-own
		 * launches
		 */
		boolean isUserValidate = !(user.getRole().equals(ADMINISTRATOR) || findUserConfigByLogin(project, user.getId()).getProjectRole()
				.sameOrHigherThan(ProjectRole.PROJECT_MANAGER));
		launches.forEach(launch -> {
			expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);

			expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
					Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(), launch.getStatus())
			);

			expect(launch.getProjectRef(), equalTo(project.getId())).verify(FORBIDDEN_OPERATION,
					"Impossible to merge launches from different projects."
			);

			if (isUserValidate) {
				expect(launch.getUserRef(), equalTo(user.getId())).verify(ACCESS_DENIED,
						"You are not an owner of launches or have less than PROJECT_MANAGER project role."
				);
			}
		});
	}

	/**
	 * Update test-items of specified launches with new LaunchID
	 */
	private void updateChildrenOfLaunches(String launchId, Set<String> launches, boolean extendDescription, boolean isNameChanged) {
		List<TestItem> testItems = launches.stream().flatMap(id -> {
			Launch launch = launchRepository.findOne(id);
			return testItemRepository.findByLaunch(launch).stream().map(item -> {
				item.setLaunchRef(launchId);
				if (isNameChanged && identifierGenerator.validate(item.getUniqueId())) {
					item.setUniqueId(identifierGenerator.generate(item));
				}
				if (item.getType().sameLevel(TestItemType.SUITE)) {
					// Add launch reference description for top level items
					Supplier<String> newDescription = Suppliers.formattedSupplier(
							((null != item.getItemDescription()) ? item.getItemDescription() : "") + (extendDescription ?
									"\r\n@launch '{} #{}'" :
									""), launch.getName(), launch.getNumber());
					item.setItemDescription(newDescription.get());
				}
				return item;
			});
		}).collect(toList());
		testItemRepository.save(testItems);
	}

	/**
	 * Create launch that will be the result of merge
	 *
	 * @param projectName
	 * @param userName
	 * @param mergeLaunchesRQ
	 * @return launch
	 */
	private Launch createResultedLaunch(String projectName, String userName, MergeLaunchesRQ mergeLaunchesRQ, boolean hasRetries,
			List<Launch> launches) {
		StartLaunchRQ startRQ = new StartLaunchRQ();
		startRQ.setMode(ofNullable(mergeLaunchesRQ.getMode()).orElse(Mode.DEFAULT));
		startRQ.setDescription(mergeLaunchesRQ.getDescription());
		startRQ.setName(ofNullable(mergeLaunchesRQ.getName()).orElse(
				"Merged: " + launches.stream().map(Launch::getName).distinct().collect(joining(", "))));
		startRQ.setTags(ofNullable(mergeLaunchesRQ.getTags()).orElse(
				launches.stream().flatMap(it -> ofNullable(it.getTags()).orElse(Collections.emptySet()).stream()).collect(toSet())));
		startRQ.setStartTime(ofNullable(mergeLaunchesRQ.getStartTime()).orElse(launches.stream()
				.sorted(comparing(Launch::getStartTime))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
				.getStartTime()));
		Launch launch = new LaunchBuilder().addStartRQ(startRQ).addProject(projectName).addStatus(IN_PROGRESS).addUser(userName).get();
		launch.setNumber(launchCounter.getLaunchNumber(launch.getName(), projectName));
		launch.setHasRetries(hasRetries ? true : null);
		return launchRepository.save(launch);
	}
}
