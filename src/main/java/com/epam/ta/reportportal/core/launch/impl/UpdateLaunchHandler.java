/*
 * Copyright 2016 EPAM Systems
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

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.database.entity.ProjectRole.*;
import static com.epam.ta.reportportal.database.entity.Status.IN_PROGRESS;
import static com.epam.ta.reportportal.database.entity.user.UserRole.ADMINISTRATOR;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.launch.IUpdateLaunchHandler;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Project.UserConfig;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.launch.AutoAnalyzeStrategy;
import com.epam.ta.reportportal.database.entity.project.ProjectUtils;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.util.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.ws.converter.LaunchResourceAssembler;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import javax.inject.Provider;

/**
 * Default implementation of {@link IUpdateLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateLaunchHandler implements IUpdateLaunchHandler {

	@Autowired
	private TestItemRepository testItemRepository;

	private ProjectRepository projectRepository;
	private LaunchRepository launchRepository;
	private UserRepository userRepository;

	@Autowired
	private Provider<LaunchBuilder> launchBuilder;

	@Autowired
	private LaunchMetaInfoRepository launchCounter;

	@Autowired
	private LaunchResourceAssembler launchResourceAssembler;

	@Autowired
	private IIssuesAnalyzer analyzerService;

	@Autowired
	@Qualifier("autoAnalyzeTaskExecutor")
	private TaskExecutor taskExecutor;

	@Value("${rp.issue.analyzer.depth}")
	private Integer autoAnalysisDepth;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Autowired
	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS updateLaunch(String launchId, String projectName, String userName, UpdateLaunchRQ rq) {
		Launch launch = launchRepository.findOne(launchId);
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);
		validate(launch, userName, projectName, rq.getMode());
		if ((null != rq.getMode()) || (null != rq.getDescription()) || (null != rq.getTags())) {
			if (null != rq.getMode()) {
				launch.setMode(rq.getMode());
			}
			if (null != rq.getDescription()) {
				launch.setDescription(rq.getDescription().trim());
			}
			if (null != rq.getTags()) {
				launch.setTags(Sets.newHashSet(EntityUtils.trimStrings(EntityUtils.update(rq.getTags()))));
			}
			launchRepository.save(launch);
		}
		return new OperationCompletionRS("Launch with ID = '" + launch.getId() + "' successfully updated.");
	}

	@Override
	public LaunchResource mergeLaunches(String projectName, String userName, MergeLaunchesRQ mergeLaunchesRQ) {
		User user = userRepository.findOne(userName);
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		Set<String> launchesIds = mergeLaunchesRQ.getLaunches();
		List<Launch> launchesList = launchRepository.find(launchesIds);

		validateMergingLaunches(launchesList, user, project);

		StartLaunchRQ startRQ = new StartLaunchRQ();
		startRQ.setMode(mergeLaunchesRQ.getMode());
		startRQ.setDescription(mergeLaunchesRQ.getDescription());
		startRQ.setName(mergeLaunchesRQ.getName());
		startRQ.setTags(mergeLaunchesRQ.getTags());

		launchesList.sort(Comparator.comparing(Launch::getStartTime));
		startRQ.setStartTime(launchesList.get(0).getStartTime());
		Launch launch = launchBuilder.get().addStartRQ(startRQ).addProject(projectName).addStatus(IN_PROGRESS).addUser(userName).build();
		launch.setNumber(launchCounter.getLaunchNumber(launch.getName(), projectName));
		launchRepository.save(launch);

		launch = launchRepository.findOne(launch.getId());
		launch.setEndTime(launchesList.get(launchesList.size() - 1).getEndTime());
		List<TestItem> statisticsBase = updateChildrenOfLaunch(launch.getId(), mergeLaunchesRQ.getLaunches(),
				mergeLaunchesRQ.isExtendSuitesDescription());
		launch.setStatistics(getLaunchStatisticFromItems(statisticsBase));
		launch.setStatus(getStatusFromStatistics(launch.getStatistics()));
		launchRepository.save(launch);

		launchRepository.delete(launchesIds);

		return launchResourceAssembler.toResource(launch);
	}

	@Override
	// TODO Review after all new requirements BRs list and optimize it
	public OperationCompletionRS startLaunchAnalyzer(String projectName, String launchId, String scope) {
		AutoAnalyzeStrategy type = AutoAnalyzeStrategy.fromValue(scope);
		expect(type, notNull()).verify(INCORRECT_FILTER_PARAMETERS, scope);

		Launch launch = launchRepository.findOne(launchId);
		expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launchId);

		expect(launch.getProjectRef(), equalTo(projectName)).verify(FORBIDDEN_OPERATION,
				Suppliers.formattedSupplier("Launch with ID '{}' is not under '{}' project.", launchId, projectName));

		/* Do not process debug launches */
		expect(launch.getMode(), equalTo(DEFAULT)).verify(INCORRECT_REQUEST, "Cannot analyze launches in debug mode.");

		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		/* Prevent AA for already processing launches */
		if (!analyzerService.isPossible(launchId) && type.equals(AutoAnalyzeStrategy.HISTORY)) {
			fail().withError(FORBIDDEN_OPERATION,
					Suppliers.formattedSupplier("Launch with ID '{}' in auto-analyzer cache already", launchId));
		}

		/*
		 * Stupid requirement -> for IN_PROGRESS launches: AA is possible, but
		 * Match is not
		 */
		if ((launch.getStatus().equals(IN_PROGRESS) || !analyzerService.isPossible(launchId))
				&& !(type.equals(AutoAnalyzeStrategy.HISTORY)))
			fail().withError(FORBIDDEN_OPERATION,
					Suppliers.formattedSupplier("Launch with ID '{}' in auto-analyzer cache already and/or in progress still", launchId));

		List<TestItem> toInvestigate = testItemRepository.findInIssueTypeItems(TestItemIssueType.TO_INVESTIGATE.getLocator(), launchId);
		List<TestItem> got;
		if (type.equals(AutoAnalyzeStrategy.SINGLE)) {
			/* Match issues for single launch */
			got = analyzerService.collectPreviousIssues(1, launchId, projectName);
		} else {
			/* General AA flow */
			got = analyzerService.collectPreviousIssues(autoAnalysisDepth, launchId, projectName);
		}

		if (analyzerService.analyzeStarted(launchId)) {
			taskExecutor.execute(() -> analyzerService.analyze(launchId, toInvestigate, got));
		}
		return new OperationCompletionRS("Auto-analyzer for launch ID='" + launchId + "' started.");
	}

	@Override
	public List<OperationCompletionRS> updateLaunch(BulkRQ<UpdateLaunchRQ> rq, String projectName, String userName) {
		return rq.getEntities().entrySet().stream().map(entry -> updateLaunch(entry.getKey(), projectName, userName, entry.getValue()))
				.collect(toList());
	}

	/**
	 * Validations for merge launches request parameters and data
	 *
	 * @param launches
	 */
	private void validateMergingLaunches(List<Launch> launches, User user, Project project) {
		expect(launches.size(), not(equalTo(0))).verify(BAD_REQUEST_ERROR, launches);

		/*
		 * ADMINISTRATOR and LEAD+ users have permission to merge not-only-own
		 * launches
		 */
		boolean isUserValidate = !(user.getRole().equals(ADMINISTRATOR)
				|| project.getUsers().get(user.getId()).getProjectRole().getRoleLevel() >= LEAD.getRoleLevel());
		launches.stream().forEach(launch -> {
			expect(launch, notNull()).verify(LAUNCH_NOT_FOUND, launch);

			expect(analyzerService.isPossible(launch.getId()), equalTo(true)).verify(FORBIDDEN_OPERATION,
					"Impossible to merge launch which under AA processing");

			expect(launch.getStatus(), not(Preconditions.statusIn(IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
					Suppliers.formattedSupplier("Cannot merge launch '{}' with status '{}'", launch.getId(), launch.getStatus()));

			expect(launch.getProjectRef(), equalTo(project.getId())).verify(FORBIDDEN_OPERATION,
					"Impossible to merge launches from different projects.");

			if (isUserValidate) {
				expect(launch.getUserRef(), equalTo(user.getId())).verify(ACCESS_DENIED,
						"You are not an owner of launches or have less than LEAD project role.");
			}
		});
	}

	private void validate(Launch launch, String userName, String projectName, Mode mode) {
		// BusinessRule.expect(launch.getUserRef(),
		// Predicates.notNull()).verify(ErrorType.ACCESS_DENIED);
		String launchOwner = launch.getUserRef();
		User principal = userRepository.findOne(userName);
		Project project = projectRepository.findOne(projectName);
		if ((project.getUsers().get(userName).getProjectRole() == CUSTOMER) && (null != mode)) {
			expect(mode, equalTo(DEFAULT)).verify(ACCESS_DENIED);
		}
		if (principal.getRole() != ADMINISTRATOR) {
			expect(launch.getProjectRef(), equalTo(projectName)).verify(ACCESS_DENIED);
			if ((null == launchOwner) || (!launchOwner.equalsIgnoreCase(userName))) {
				/*
				 * Only LEAD and PROJECT_MANAGER roles could move launches
				 * to/from DEBUG mode
				 */
				UserConfig userConfig = project.getUsers().get(userName);
				expect(userConfig, Preconditions.hasProjectRoles(Lists.newArrayList(PROJECT_MANAGER, LEAD))).verify(ACCESS_DENIED);
			} else {
				/*
				 * Only owner could change launch mode
				 */
				expect(userName, equalTo(launchOwner)).verify(ACCESS_DENIED);
			}
		}
	}

	/**
	 * Update test-items of specified launches with new LaunchID
	 *
	 * @param launchId
	 */
	private List<TestItem> updateChildrenOfLaunch(String launchId, Set<String> launches, boolean extendDescription) {
		List<TestItem> testItems = launches.stream().map(id -> {
			Launch launch = launchRepository.findOne(id);
			return testItemRepository.findByLaunch(launch).stream().map(item -> {
				item.setLaunchRef(launchId);
				if (item.getPath().size() == 0) {
					// Add launch reference description for top level items
					Supplier<String> newDescription = Suppliers
							.formattedSupplier(((null != item.getItemDescription()) ? item.getItemDescription() : "")
									+ (extendDescription ? "\r\n@launch '{} #{}'" : ""), launch.getName(), launch.getNumber());
					item.setItemDescription(newDescription.get());
				}
				return item;
			}).collect(toList());
		}).flatMap(List::stream).collect(toList());
		testItemRepository.save(testItems);
		return testItems.stream().filter(item -> item.getPath().size() == 0).collect(toList());
	}

	/**
	 * Calculation of statistic based on set of test items
	 *
	 * @param input
	 * @return Statistics object
	 */
	private Statistics getLaunchStatisticFromItems(List<TestItem> input) {
		ExecutionCounter execution = new ExecutionCounter();
		IssueCounter issues = new IssueCounter();
		for (TestItem item : input) {
			// common execution statistics calculation
			execution.setTotal(item.getStatistics().getExecutionCounter().getTotal() + execution.getTotal());
			execution.setPassed(item.getStatistics().getExecutionCounter().getPassed() + execution.getPassed());
			execution.setFailed(item.getStatistics().getExecutionCounter().getFailed() + execution.getFailed());
			execution.setSkipped(item.getStatistics().getExecutionCounter().getSkipped() + execution.getSkipped());

			// common issues statistics calculation
			issues = ProjectUtils.sumIssueStatistics(issues, item.getStatistics().getIssueCounter());
		}
		return new Statistics(execution, issues);
	}
}