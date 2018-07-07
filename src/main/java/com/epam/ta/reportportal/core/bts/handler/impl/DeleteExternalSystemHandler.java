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

import static com.epam.ta.reportportal.ws.model.ErrorType.EXTERNAL_SYSTEM_NOT_FOUND;

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
				.orElseThrow(() -> new ReportPortalException(EXTERNAL_SYSTEM_NOT_FOUND, id));

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