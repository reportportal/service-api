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

package com.epam.ta.reportportal.core.externalsystem.handler.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.externalsystem.handler.IUpdateExternalSystemHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuth;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystemAuthFactory;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemFieldsConverter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.model.ErrorType.EXTERNAL_SYSTEM_ALREADY_EXISTS;
import static com.epam.ta.reportportal.ws.model.ErrorType.EXTERNAL_SYSTEM_NOT_FOUND;
import static java.util.Optional.ofNullable;

/**
 * Initial realization for {@link IUpdateExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
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

		/* Remember initial parameters of saved external system */
		final String sysUrl = bugTrackingSystem.getUrl();
		final String sysProject = bugTrackingSystem.getBtsProject();
		final Long rpProject = bugTrackingSystem.getProject().getId();

		ofNullable(request.getUrl()).ifPresent(it -> {
			if (request.getUrl().endsWith("/")) {
				request.setUrl(request.getUrl().substring(0, request.getUrl().length() - 1));
			}
		});

		/* Check input params for avoid external system duplication */
		if (!sysUrl.equalsIgnoreCase(request.getUrl()) || !sysProject.equalsIgnoreCase(request.getProject()) || !Objects.equals(
				rpProject, projectDetails.getProjectId())) {
			bugTrackingSystemRepository.findByUrlAndBtsProjectAndProjectId(
					request.getUrl(), request.getProject(), projectDetails.getProjectId()).ifPresent(it -> {
				throw new ReportPortalException(EXTERNAL_SYSTEM_ALREADY_EXISTS, request.getUrl() + " & " + request.getProject());
			});
		}

		bugTrackingSystem.setUrl(request.getUrl());

		if (!StringUtils.isEmpty(request.getExternalSystemType())) {
			bugTrackingSystem.setBtsType(request.getExternalSystemType());
		}

		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(bugTrackingSystem.getBtsType());

		if (StringUtils.isEmpty(request.getProject())) {
			bugTrackingSystem.setBtsProject(request.getProject());
		}
		/* Hard referenced project update */
		Project project = new Project();
		project.setId(projectDetails.getProjectId());
		bugTrackingSystem.setProject(project);

		if (!CollectionUtils.isEmpty(request.getFields())) {
			bugTrackingSystem.setDefectFormFields(request.getFields()
					.stream()
					.map(ExternalSystemFieldsConverter.FIELD_TO_DB)
					.collect(Collectors.toSet()));
		}

		if (null != request.getExternalSystemAuth()) {
			BugTrackingSystemAuth auth = bugTrackingSystemAuthFactory.createAuthObject(bugTrackingSystem.getAuth(), request);
			bugTrackingSystem.setAuth(auth);

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
		}

		bugTrackingSystemRepository.save(bugTrackingSystem);

		//eventPublisher.publishEvent(new ExternalSystemUpdatedEvent(exist, principalName));
		return new OperationCompletionRS("ExternalSystem with ID = '" + id + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS externalSystemConnect(UpdateExternalSystemRQ updateRQ, String projectName, Long systemId,
			ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		BugTrackingSystem bugTrackingSystem = bugTrackingSystemRepository.findById(systemId)
				.orElseThrow(() -> new ReportPortalException(EXTERNAL_SYSTEM_NOT_FOUND, systemId));

		//ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(updateRQ.getExternalSystemType());

		BugTrackingSystem details = new BugTrackingSystem();
		details.setUrl(updateRQ.getUrl());
		details.setBtsProject(updateRQ.getProject());
		BugTrackingSystemAuth auth = bugTrackingSystemAuthFactory.createAuthObject(bugTrackingSystem.getAuth(), updateRQ);
		details.setAuth(auth);

		//		expect(externalSystemStrategy.connectionTest(details), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
		//				system.getProjectRef()
		//		);

		return new OperationCompletionRS("Connection to ExternalSystem with ID = '" + systemId + "' is successfully performed.");
	}
}
