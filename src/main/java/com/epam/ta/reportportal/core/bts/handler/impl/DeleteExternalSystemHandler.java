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

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.IDeleteExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;

/**
 * Basic implementation for {@link IDeleteExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class DeleteExternalSystemHandler implements IDeleteExternalSystemHandler {

	@Autowired
	private BugTrackingSystemRepository bugTrackingSystemRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public synchronized OperationCompletionRS deleteExternalSystem(String projectName, Long id, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);

		BugTrackingSystem bugTrackingSystem = bugTrackingSystemRepository.findByIdAndProjectId(id, projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, id));

		bugTrackingSystemRepository.delete(bugTrackingSystem);

		//eventPublisher.publishEvent(new ExternalSystemDeletedEvent(exist, username));
		return new OperationCompletionRS("ExternalSystem with ID = '" + id + "' is successfully deleted.");
	}

	@Override
	public synchronized OperationCompletionRS deleteAllExternalSystems(String projectName, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		List<BugTrackingSystem> btsSystems = bugTrackingSystemRepository.findAllByProjectId(projectDetails.getProjectId());
		if (!CollectionUtils.isEmpty(btsSystems)) {
			bugTrackingSystemRepository.deleteAll(btsSystems);
		}
		//eventPublisher.publishEvent(new ProjectExternalSystemsDeletedEvent(exist, projectName, username));
		return new OperationCompletionRS("All ExternalSystems for project '" + projectName + "' successfully removed");
	}
}