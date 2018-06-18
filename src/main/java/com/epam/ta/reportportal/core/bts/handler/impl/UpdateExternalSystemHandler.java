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
import com.epam.ta.reportportal.core.bts.handler.IUpdateExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuthFactory;
import com.epam.ta.reportportal.ws.converter.builders.BugTrackingSystemBuilder;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemFieldsConverter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.model.ErrorType.EXTERNAL_SYSTEM_NOT_FOUND;

/**
 * Initial realization for {@link IUpdateExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
@Service
public class UpdateExternalSystemHandler implements IUpdateExternalSystemHandler {

	//	@Autowired
	//	private StrategyProvider strategyProvider;

	@Autowired
	private BugTrackingSystemRepository bugTrackingSystemRepository;

	@Autowired
	private BugTrackingSystemAuthFactory bugTrackingSystemAuthFactory;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ request, String projectName, Long id, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		BugTrackingSystem bugTrackingSystem = bugTrackingSystemRepository.findById(id)
				.orElseThrow(() -> new ReportPortalException(EXTERNAL_SYSTEM_NOT_FOUND, id));

		BugTrackingSystemBuilder builder = new BugTrackingSystemBuilder(bugTrackingSystem);

		bugTrackingSystem = builder.addUrl(request.getUrl())
				.addBugTrackingSystemType(request.getExternalSystemType())
				.addBugTrackingProject(request.getProject())
				.addProject(projectDetails.getProjectId())
				.addFields(request.getFields().stream().map(ExternalSystemFieldsConverter.FIELD_TO_DB).collect(Collectors.toSet()))
				.get();

		//TODO probably could be handled by database
//				/* Check input params for avoid external system duplication */
//		if (!sysUrl.equalsIgnoreCase(bugTrackingSystem.getUrl()) || !sysProject.equalsIgnoreCase(bugTrackingSystem.getBtsProject())
//				|| !Objects.equals(rpProject, projectDetails.getProjectId())) {
//			bugTrackingSystemRepository.findByUrlAndBtsProjectAndProjectId(
//					request.getUrl(), request.getProject(), projectDetails.getProjectId()).ifPresent(it -> {
//				throw new ReportPortalException(EXTERNAL_SYSTEM_ALREADY_EXISTS, request.getUrl() + " & " + request.getProject());
//			});
//		}

		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(bugTrackingSystem.getBtsType());

		//			if (authType.requiresPassword()) {
		//				String decrypted = bugTrackingSystem.getAuth();
		//				exist.setPassword(simpleEncryptor.decrypt(exist.getPassword()));
		//				expect(externalSystemStrategy.connectionTest(exist), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
		//						projectName
		//				);
		//				exist.setPassword(decrypted);
		//			} else {
		//				expect(externalSystemStrategy.connectionTest(exist), equalTo(true)).verify(
		//						UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, projectName);
		//			}

		bugTrackingSystemRepository.save(bugTrackingSystem);

		//eventPublisher.publishEvent(new IntegrationUpdatedEvent(exist, principalName));
		return new OperationCompletionRS("ExternalSystem with ID = '" + id + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS externalSystemConnect(UpdateExternalSystemRQ updateRQ, String projectName, Long systemId,
			ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		BugTrackingSystem bugTrackingSystem = bugTrackingSystemRepository.findById(systemId)
				.orElseThrow(() -> new ReportPortalException(EXTERNAL_SYSTEM_NOT_FOUND, systemId));

		//ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(updateRQ.getExternalSystemType());

		BugTrackingSystem details = new BugTrackingSystemBuilder().addUrl(updateRQ.getUrl())
				.addBugTrackingProject(updateRQ.getProject())
				.get();

		//		expect(externalSystemStrategy.connectionTest(details), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
		//				system.getProjectRef()
		//		);

		return new OperationCompletionRS("Connection to ExternalSystem with ID = '" + systemId + "' is successfully performed.");
	}
}
