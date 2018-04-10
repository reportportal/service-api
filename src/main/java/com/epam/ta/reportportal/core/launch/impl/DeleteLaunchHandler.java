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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.IDeleteLaunchHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.user.UserRole;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.store.commons.Predicates.not;
import static com.epam.ta.reportportal.store.database.entity.project.ProjectRole.PROJECT_MANAGER;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Arrays.asList;

/**
 * Default implementation of {@link IDeleteLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class DeleteLaunchHandler implements IDeleteLaunchHandler {

	private ApplicationEventPublisher eventPublisher;

	private LaunchRepository launchRepository;

	//	private ILogIndexer logIndexer;

	@Autowired
	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	//	@Autowired
	//	public void setLogIndexer(ILogIndexer logIndexer) {
	//		this.logIndexer = logIndexer;
	//	}

	//TODO Analyzer, Activities
	public OperationCompletionRS deleteLaunch(Long launchId, String projectName, ReportPortalUser user) {
		Launch launch = launchRepository.findById(launchId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
		validate(launch, user, projectName);
		launchRepository.delete(launch);

		//		logIndexer.cleanIndex(
		//				projectName, itemRepository.selectIdsNotInIssueByLaunch(launchId, TestItemIssueType.TO_INVESTIGATE.getLocator()));
		//		eventPublisher.publishEvent(new LaunchDeletedEvent(launch, principal));
		return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully deleted.");
	}

	//TODO Analyzer, Activities
	public OperationCompletionRS deleteLaunches(Long[] ids, String projectName, ReportPortalUser user) {
		List<Launch> launches = launchRepository.findAllById(asList(ids));
		launches.forEach(l -> validate(l, user, projectName));
		//		launches.forEach(launch -> logIndexer.cleanIndex(projectName,
		//				itemRepository.selectIdsNotInIssueByLaunch(launch.getId(), TestItemIssueType.TO_INVESTIGATE.getLocator())
		//		));
		launchRepository.deleteAll(launches);

		//		launches.forEach(launch -> eventPublisher.publishEvent(new LaunchDeletedEvent(launch, userName)));
		return new OperationCompletionRS("All selected launches have been successfully deleted");
	}

	private void validate(Launch launch, ReportPortalUser user, String projectName) {
		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("FilterTarget launch '{}' not under specified project '{}'", launch.getId(), projectName)
		);
		expect(launch, not(l -> l.getStatus().equals(StatusEnum.IN_PROGRESS))).verify(LAUNCH_IS_NOT_FINISHED,
				formattedSupplier("Unable to delete launch '{}' in progress state", launch.getId())
		);
		if (user.getUserRole() != UserRole.ADMINISTRATOR && !Objects.equals(user.getUserId(), launch.getUserId())) {
			/* Only PROJECT_MANAGER roles could delete launches */
			expect(projectDetails.getProjectRole(), equalTo(PROJECT_MANAGER)).verify(ACCESS_DENIED);
		}
	}
}