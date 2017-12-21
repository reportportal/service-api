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

package com.epam.ta.reportportal.core.externalsystem.handler.impl;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.externalsystem.handler.IDeleteExternalSystemHandler;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.events.ExternalSystemDeletedEvent;
import com.epam.ta.reportportal.events.ProjectExternalSystemsDeletedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Basic implementation for {@link IDeleteExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteExternalSystemHandler implements IDeleteExternalSystemHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public synchronized OperationCompletionRS deleteExternalSystem(String projectName, String id, String username) {
		Project project = projectRepository.findByName(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		ExternalSystem exist = externalSystemRepository.findOne(id);
		expect(exist, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, exist);

		if (!project.getConfiguration().getExternalSystem().contains(id)) {
			fail().withError(FORBIDDEN_OPERATION,
					Suppliers.formattedSupplier("BTS with ID='{}' doesn't project '{}' related", id, projectName)
			);
		}

		try {
			externalSystemRepository.delete(id);
			List<String> externalSystemIds = project.getConfiguration().getExternalSystem();
			externalSystemIds.remove(id);
			project.getConfiguration().setExternalSystem(externalSystemIds);
			projectRepository.save(project);
		} catch (Exception e) {
			throw new ReportPortalException("Error during deleting ExternalSystem", e);
		}
		eventPublisher.publishEvent(new ExternalSystemDeletedEvent(exist, username));
		return new OperationCompletionRS("ExternalSystem with ID = '" + id + "' is successfully deleted.");
	}

	@Override
	public synchronized OperationCompletionRS deleteAllExternalSystems(String projectName, String username) {
		Project project = projectRepository.findByName(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		Iterable<ExternalSystem> exist = externalSystemRepository.findByProjectRef(projectName);
		try {
			if (null != exist) {
				externalSystemRepository.delete(exist);
			}
			/*
			 * Cannot use set of empty array here cause parallel operations with
			 * getProject() for DELETE ExtSys and PostExtSys save cleared data
			 * back. Direct mongoOperations using instead.
			 */
			// FIXME Review after ThomsonReuters release with -> client-options
			// write-concern="ACKNOWLEDGED"
			projectRepository.clearExternalSystems(projectName);

			eventPublisher.publishEvent(new ProjectExternalSystemsDeletedEvent(exist, projectName, username));
		} catch (Exception e) {
			throw new ReportPortalException("Clean: error during deleting ExternalSystems", e);
		}
		return new OperationCompletionRS("All ExternalSystems for project '" + projectName + "' successfully removed");
	}
}