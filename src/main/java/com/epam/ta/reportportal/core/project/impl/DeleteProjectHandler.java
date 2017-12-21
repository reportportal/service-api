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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.project.IDeleteProjectHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static java.util.Collections.singletonList;

/**
 * Initial implementation of
 * {@link com.epam.ta.reportportal.core.project.IDeleteProjectHandler}
 *
 * @author Hanna_Sukhadolava
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteProjectHandler implements IDeleteProjectHandler {

	private final ProjectRepository projectRepository;

	@Autowired
	private ILogIndexer logIndexer;

	@Autowired
	public DeleteProjectHandler(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS deleteProject(String projectName) {

		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		expect(project.getConfiguration().getEntryType(), equalTo(EntryType.INTERNAL)).verify(
				ErrorType.PROJECT_UPDATE_NOT_ALLOWED, project.getConfiguration().getEntryType());
		try {
			projectRepository.delete(singletonList(projectName));
			logIndexer.deleteIndex(projectName);
		} catch (Exception e) {
			throw new ReportPortalException("Error during deleting Project and attributes", e);
		}
		return new OperationCompletionRS("Project with name = '" + projectName + "' is successfully deleted.");
	}
}
