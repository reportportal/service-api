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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.launch.IUpdateLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType;
import com.epam.ta.reportportal.store.database.entity.item.TestItemCommon;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.INCORRECT_REQUEST;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Default implementation of {@link IUpdateLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateLaunchHandler implements IUpdateLaunchHandler {

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	private IIssuesAnalyzer analyzerService;

	private ILogIndexer logIndexer;

	//	@Autowired
	//	//@Qualifier("autoAnalyzeTaskExecutor")
	//	private TaskExecutor taskExecutor;

	//	@Autowired
	//	public void setAnalyzerService(IIssuesAnalyzer analyzerService) {
	//		this.analyzerService = analyzerService;
	//	}
	//
	//	@Autowired
	//	public void setLogIndexer(ILogIndexer logIndexer) {
	//		this.logIndexer = logIndexer;
	//	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	//	@Autowired
	//	public void setUserRepository(UserRepository userRepository) {
	//		this.userRepository = userRepository;
	//	}
	//
	//	@Autowired
	//	public void setProjectRepository(ProjectRepository projectRepository) {
	//		this.projectRepository = projectRepository;
	//	}

	public OperationCompletionRS updateLaunch(Long launchId, String projectName, String userName, UpdateLaunchRQ rq) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));

		//TODO Replace validation with new uat
		//validate(launch, userName, projectName, rq.getMode());

		ofNullable(rq.getMode()).ifPresent(mode -> launch.setMode(LaunchModeEnum.valueOf(rq.getMode().name())));
		ofNullable(rq.getDescription()).ifPresent(launch::setDescription);
		Set<String> set = Sets.newHashSet(EntityUtils.trimStrings(rq.getTags()));
		if (!CollectionUtils.isEmpty(set)) {
			launch.getTags().clear();
			launch.getTags().addAll(set.stream().map(LaunchTag::new).collect(toSet()));
		}
		reindexLogs(launch);
		launchRepository.save(launch);
		return new OperationCompletionRS("Launch with ID = '" + launch.getId() + "' successfully updated.");
	}

	public OperationCompletionRS startLaunchAnalyzer(String projectName, Long launchId) {
		expect(analyzerService.hasAnalyzers(), Predicate.isEqual(true)).verify(
				ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, "There are no analyzer services are deployed.");

		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));

		/* Do not process debug launches */
		expect(launch.getMode(), equalTo(DEFAULT)).verify(INCORRECT_REQUEST, "Cannot analyze launches in debug mode.");

		//TODO Refactor with new uat
		//				expect(launch.getProjectRef(), equalTo(projectName)).verify(FORBIDDEN_OPERATION,
		//						Suppliers.formattedSupplier("Launch with ID '{}' is not under '{}' project.", launchId, projectName)
		//				);
		//				Project project = projectRepository.findOne(projectName);
		//				expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		List<TestItemCommon> toInvestigate = testItemRepository.selectItemsInIssueByLaunch(
				launchId, TestItemIssueType.TO_INVESTIGATE.getLocator());

		//taskExecutor.execute(() -> analyzerService.analyze(launch, toInvestigate));

		return new OperationCompletionRS("Auto-analyzer for launch ID='" + launchId + "' started.");
	}

	public List<OperationCompletionRS> updateLaunch(BulkRQ<UpdateLaunchRQ> rq, String projectName, String userName) {
		return rq.getEntities()
				.entrySet()
				.stream().map(entry -> updateLaunch(Long.valueOf(entry.getKey()), projectName, userName, entry.getValue()))
				.collect(toList());
	}

	/**
	 * If launch mode has changed - reindex items
	 *
	 * @param launch Update launch
	 */
	private void reindexLogs(Launch launch) {
		List<Long> itemIds = testItemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueType.TO_INVESTIGATE.getLocator());
		if (!CollectionUtils.isEmpty(itemIds)) {
			if (Mode.DEBUG.name().equals(launch.getMode().name())) {

				logIndexer.cleanIndex(launch.getName(), itemIds);
			} else {
				logIndexer.indexLogs(launch.getId(), testItemRepository.findAllById(itemIds));
			}
		}
	}

	private void validate(Launch launch, String userName, String projectName, LaunchModeEnum mode) {
		//		// BusinessRule.expect(launch.getUserRef(),
		//		// Predicates.notNull()).verify(ErrorType.ACCESS_DENIED);
		//		String launchOwner = launch.getUserRef();
		//		User principal = userRepository.findOne(userName);
		//		Project project = projectRepository.findOne(projectName);
		//		if ((findUserConfigByLogin(project, userName).getProjectRole() == CUSTOMER) && (null != mode)) {
		//			expect(mode, equalTo(DEFAULT)).verify(ACCESS_DENIED);
		//		}
		//		if (principal.getRole() != ADMINISTRATOR) {
		//			expect(launch.getProjectRef(), equalTo(projectName)).verify(ACCESS_DENIED);
		//			if ((null == launchOwner) || (!launchOwner.equalsIgnoreCase(userName))) {
		//				/*
		//				 * Only PROJECT_MANAGER roles could move launches
		//				 * to/from DEBUG mode
		//				 */
		//				UserConfig userConfig = findUserConfigByLogin(project, userName);
		//				expect(userConfig, Preconditions.hasProjectRoles(singletonList(PROJECT_MANAGER))).verify(ACCESS_DENIED);
		//			} else {
		//				/*
		//				 * Only owner could change launch mode
		//				 */
		//				expect(userName, equalTo(launchOwner)).verify(ACCESS_DENIED);
		//			}
		//		}
	}

}