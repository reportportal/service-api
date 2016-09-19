/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.core.project.impl;

import java.util.List;

import com.epam.ta.reportportal.commons.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.project.IDeleteProjectHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

/**
 * Initial implementation of
 * {@link com.epam.ta.reportportal.core.project.IDeleteProjectHandler}
 * 
 * @author Hanna_Sukhadolava
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteProjectHandler implements IDeleteProjectHandler {

	@Autowired
	private ProjectRepository projectRepository;

	/*
	 * TODO may be put in triggers after stabilizing of project data for
	 * removing functionality
	 */
	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Override
	public OperationCompletionRS deleteProject(String projectName) {
		BusinessRule.expect(projectName, Predicates.not(Predicates.equalTo(Constants.DEFAULT_PROJECT.toString())))
				.verify(ErrorType.UNABLE_TO_UPDATE_DEFAULT_PROJECT);

		Project project = projectRepository.findOne(projectName);
		BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.PROJECT_NOT_FOUND, projectName);
		Iterable<ExternalSystem> externalSystems = externalSystemRepository.findAll(project.getConfiguration().getExternalSystem());
		List<Dashboard> dashes = dashboardRepository.findByProject(projectName);

		try {
			projectRepository.delete(projectName);
			externalSystemRepository.delete(externalSystems);
			dashboardRepository.delete(dashes);
		} catch (Exception e) {
			throw new ReportPortalException("Error during deleting Project and attributes", e);
		}

		OperationCompletionRS response = new OperationCompletionRS();
		StringBuilder msg = new StringBuilder();
		msg.append("Project with name = '");
		msg.append(projectName);
		msg.append("' is successfully deleted.");
		response.setResultMessage(msg.toString());
		return response;
	}
}