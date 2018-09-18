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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.item.impl.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.MergeStrategyFactory;
import com.epam.ta.reportportal.core.item.impl.merge.strategy.MergeStrategyType;
import com.epam.ta.reportportal.core.item.merge.MergeStrategy;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.launch.LaunchTag;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
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

import java.util.*;
import java.util.function.Supplier;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.IN_PROGRESS;
import static com.epam.ta.reportportal.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

/**
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
@Service
public class MergeLaunchHandler implements com.epam.ta.reportportal.core.launch.MergeLaunchHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private MergeStrategyFactory mergeStrategyFactory;

	//	@Autowired
	//	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private TestItemUniqueIdGenerator identifierGenerator;

	//	@Autowired
	//	private ILogIndexer logIndexer;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Override
	public LaunchResource mergeLaunches(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, MergeLaunchesRQ rq) {
		//TODO: analyzer, statistics
		//		User user = userRepository.findByLogin(user.getLogin());
		Optional<Project> projectOptional = projectRepository.findById(projectDetails.getProjectId());
		expect(projectOptional, isPresent()).verify(PROJECT_NOT_FOUND, projectDetails.getProjectId());

		Set<Long> launchesIds = rq.getLaunches();
		List<Launch> launchesList = launchRepository.findAllById(launchesIds);
		validateMergingLaunches(launchesList, user, projectDetails);

		Launch newLaunch = createResultedLaunch(projectDetails.getProjectId(), user.getUserId(), rq, launchesList);
		boolean isNameChanged = !newLaunch.getName().equals(launchesList.get(0).getName());
		updateChildrenOfLaunches(newLaunch, rq.getLaunches(), rq.isExtendSuitesDescription(), isNameChanged);

		MergeStrategyType type = MergeStrategyType.fromValue(rq.getMergeStrategyType());
		expect(type, notNull()).verify(UNSUPPORTED_MERGE_STRATEGY_TYPE, type);

		// deep merge strategies
		if (!type.equals(MergeStrategyType.BASIC)) {
			MergeStrategy strategy = mergeStrategyFactory.getStrategy(type);
			//  group items by unique id
			//TODO fix test-items of new launch
			testItemRepository.selectAllDescendants(newLaunch.getId())
					.stream()
					.collect(groupingBy(TestItem::getUniqueId))
					.entrySet()
					.stream()
					.map(Map.Entry::getValue)
					.filter(testItems -> testItems.size() > 1)
					.forEach(testItems -> strategy.mergeTestItems(testItems.get(0), testItems.subList(1, testItems.size())));
		}

		//		StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration()
		//				.getStatisticsCalculationStrategy());
		//		statisticsFacade.recalculateStatistics(newLaunch);

		//		newLaunch = launchRepository.findById(newLaunch.getId());
		//		newLaunch.setStatus(StatisticsHelper.getStatusFromStatistics(newLaunch.getStatistics()));
		//		newLaunch.setEndTime(rq.getEndTime());

		launchRepository.save(newLaunch);
		//		launchRepository.deleteAllById(launchesIds);

		//		logIndexer.indexLogs(newLaunch.getId(), testItemRepository.findItemsNotInIssueType(TO_INVESTIGATE.getLocator(), newLaunch.getId()));

		return LaunchConverter.TO_RESOURCE.apply(newLaunch);
	}

	/**
	 * Validations for merge launches request parameters and data
	 *
	 * @param launches
	 */
	private void validateMergingLaunches(List<Launch> launches, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		expect(launches.size(), not(equalTo(0))).verify(BAD_REQUEST_ERROR, launches);

		/*
		 * ADMINISTRATOR and PROJECT_MANAGER+ users have permission to merge not-only-own
		 * launches
		 */
		boolean isUserValidate = !(user.getUserRole().equals(ADMINISTRATOR) || projectDetails.getProjectRole()
				.sameOrHigherThan(ProjectRole.PROJECT_MANAGER));

		launches.forEach(launch -> {
			expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);

			expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
					Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(), launch.getStatus())
			);

			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
					"Impossible to merge launches from different projects."
			);

			if (isUserValidate) {
				expect(launch.getUserId(), equalTo(user.getUserId())).verify(ACCESS_DENIED,
						"You are not an owner of launches or have less than PROJECT_MANAGER project role."
				);
			}
		});
	}

	/**
	 * Update test-items of specified launches with new LaunchID
	 */
	private void updateChildrenOfLaunches(Launch newLaunch, Set<Long> launches, boolean extendDescription, boolean isNameChanged) {
		List<TestItem> testItems = launches.stream().flatMap(id -> {
			Launch launch = launchRepository.findById(id).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, id));
			return testItemRepository.findTestItemsByLaunchId(launch.getId()).stream().peek(testItem -> {
				testItem.setLaunch(newLaunch);
				if (isNameChanged && identifierGenerator.validate(testItem.getUniqueId())) {
					testItem.setUniqueId(identifierGenerator.generate(testItem, newLaunch));
				}
				if (testItem.getType().sameLevel(TestItemTypeEnum.SUITE)) {
					// Add launch reference description for top level items
					Supplier<String> newDescription = Suppliers.formattedSupplier(((null != testItem.getDescription()) ? testItem.getDescription() : "") + (extendDescription ?
									"\r\n@launch '{} #{}'" :
									""),
							launch.getName(),
							launch.getNumber()
					);
					testItem.setDescription(newDescription.get());
				}
			});
		}).collect(toList());
		testItemRepository.saveAll(testItems);
	}

	/**
	 * Create launch that will be the result of merge
	 *
	 * @param projectId
	 * @param userId
	 * @param mergeLaunchesRQ
	 * @param launches
	 * @return launch
	 */
	private Launch createResultedLaunch(Long projectId, Long userId, MergeLaunchesRQ mergeLaunchesRQ, List<Launch> launches) {
		Date startTime = ofNullable(mergeLaunchesRQ.getStartTime()).orElse(EntityUtils.TO_DATE.apply(launches.stream()
				.min(Comparator.comparing(Launch::getStartTime))
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
				.getStartTime()));
		Date endTime = ofNullable(mergeLaunchesRQ.getEndTime()).orElse(EntityUtils.TO_DATE.apply(launches.stream()
				.max(Comparator.comparing(Launch::getEndTime))
				.orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Invalid launches"))
				.getStartTime()));
		expect(endTime, time -> !time.before(startTime)).verify(ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME);

		StartLaunchRQ startRQ = new StartLaunchRQ();
		startRQ.setMode(ofNullable(mergeLaunchesRQ.getMode()).orElse(Mode.DEFAULT));
		startRQ.setDescription(ofNullable(mergeLaunchesRQ.getDescription()).orElse(launches.stream()
				.map(Launch::getDescription)
				.collect(joining("\n"))));
		startRQ.setName(ofNullable(mergeLaunchesRQ.getName()).orElse(
				"Merged: " + launches.stream().map(Launch::getName).distinct().collect(joining(", "))));
		startRQ.setTags(ofNullable(mergeLaunchesRQ.getTags()).orElse(launches.stream()
				.flatMap(launch -> ofNullable(launch.getTags()).orElse(Collections.emptySet()).stream())
				.map(LaunchTag::getValue)
				.collect(toSet())));
		startRQ.setStartTime(startTime);
		Launch launch = new LaunchBuilder().addStartRQ(startRQ)
				.addProject(projectId)
				.addStatus(IN_PROGRESS.name())
				.addUser(userId)
				.addEndTime(endTime)
				.get();
		return launchRepository.save(launch);
	}
}
