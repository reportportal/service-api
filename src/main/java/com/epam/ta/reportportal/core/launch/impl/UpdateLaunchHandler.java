/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.ACCESS_DENIED;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;
import static java.util.stream.Collectors.toList;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.UpdateLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateLaunchHandler implements com.epam.ta.reportportal.core.launch.UpdateLaunchHandler {

	private LaunchRepository launchRepository;

	//	private IssuesAnalyzer analyzerService;
	//
	//	private ILogIndexer logIndexer;

	//	@Autowired
	//	//@Qualifier("autoAnalyzeTaskExecutor")
	//	private TaskExecutor taskExecutor;

	//	@Autowired
	//	public void setAnalyzerService(IssuesAnalyzer analyzerService) {
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

	@Override
	public OperationCompletionRS updateLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user,
			UpdateLaunchRQ rq) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId.toString()));
		validate(launch, user, projectDetails, rq.getMode());
		launch = new LaunchBuilder(launch).addMode(rq.getMode()).addDescription(rq.getDescription()).addTags(rq.getTags()).get();
		//reindexLogs(launch);
		launchRepository.save(launch);
		return new OperationCompletionRS("Launch with ID = '" + launch.getId() + "' successfully updated.");
	}

	@Override
	public List<OperationCompletionRS> updateLaunch(BulkRQ<UpdateLaunchRQ> rq, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		return rq.getEntities()
				.entrySet()
				.stream()
				.map(entry -> updateLaunch(entry.getKey(), projectDetails, user, entry.getValue()))
				.collect(toList());
	}

	@Override
	public OperationCompletionRS startLaunchAnalyzer(ReportPortalUser.ProjectDetails projectDetails, Long launchId) {
		//		//		expect(analyzerService.hasAnalyzers(), Predicate.isEqual(true)).verify(
		//		//				ErrorType.UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, "There are no analyzer services are deployed.");
		//
		//		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
		//
		//		/* Do not process debug launches */
		//		expect(launch.getMode(), equalTo(LaunchModeEnum.DEFAULT)).verify(INCORRECT_REQUEST, "Cannot analyze launches in debug mode.");
		//
		//		//TODO Refactor with new uat
		//		//				expect(launch.getProjectRef(), equalTo(projectName)).verify(FORBIDDEN_OPERATION,
		//		//						Suppliers.formattedSupplier("Launch with ID '{}' is not under '{}' project.", launchId, projectName)
		//		//				);
		//		//				Project project = projectRepository.findOne(projectName);
		//		//				expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		//
		//		List<TestItem> toInvestigate = testItemRepository.selectItemsInIssueByLaunch(
		//				launchId, TestItemIssueType.TO_INVESTIGATE.getLocator());
		//
		//		//taskExecutor.execute(() -> analyzerService.analyze(launch, toInvestigate));
		//
		//		return new OperationCompletionRS("Auto-analyzer for launch ID='" + launchId + "' started.");
		throw new UnsupportedOperationException("Auto-analyzer starting is not implemented.");
	}

	//	/**
	//	 * If launch mode has changed - reindex items
	//	 *
	//	 * @param launch Update launch
	//	 */
	//	private void reindexLogs(Launch launch) {
	//		List<Long> itemIds = testItemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueType.TO_INVESTIGATE.getLocator());
	//		if (!CollectionUtils.isEmpty(itemIds)) {
	//			if (Mode.DEBUG.name().equals(launch.getMode().name())) {
	//
	//				logIndexer.cleanIndex(launch.getName(), itemIds);
	//			} else {
	//				logIndexer.indexLogs(launch.getId(), testItemRepository.findAllById(itemIds));
	//			}
	//		}
	//	}

	/**
	 * Valide {@link ReportPortalUser} credentials
	 *
	 * @param launch {@link Launch}
	 * @param user   {@link ReportPortalUser}
	 * @param mode   {@link Launch#mode}
	 */
	private void validate(Launch launch, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails, Mode mode) {
		if (projectDetails.getProjectRole() == ProjectRole.CUSTOMER && null != mode) {
			expect(mode, equalTo(Mode.DEFAULT)).verify(ACCESS_DENIED);
		}
		if (user.getUserRole() != UserRole.ADMINISTRATOR) {
			expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
			if (!Objects.equals(launch.getUser().getLogin(), user.getUsername())) {
				/*
				 * Only PROJECT_MANAGER roles could move launches
				 * to/from DEBUG mode
				 */
				expect(projectDetails.getProjectRole(), equalTo(PROJECT_MANAGER)).verify(ACCESS_DENIED);
			}
		}
	}

}