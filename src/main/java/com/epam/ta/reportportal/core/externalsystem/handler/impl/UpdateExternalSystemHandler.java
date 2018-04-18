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

import com.epam.ta.reportportal.core.externalsystem.ExternalSystemStrategy;
import com.epam.ta.reportportal.core.externalsystem.StrategyProvider;
import com.epam.ta.reportportal.core.externalsystem.handler.IUpdateExternalSystemHandler;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.AuthType;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.events.ExternalSystemUpdatedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Initial realization for {@link IUpdateExternalSystemHandler} interface
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class UpdateExternalSystemHandler implements IUpdateExternalSystemHandler {

	@Autowired
	private StrategyProvider strategyProvider;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ request, String projectName, String id, String principalName) {
		Project project = projectRepository.findByName(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		ExternalSystem exist = externalSystemRepository.findOne(id);
		expect(exist, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, exist);

		/* Remember initial parameters of saved external system */
		final String sysUrl = exist.getUrl();
		final String sysProject = exist.getProject();
		final String rpProject = exist.getProjectRef();

		if (null != request.getUrl()) {
			/* Remove trailing slash */
			if (request.getUrl().endsWith("/")) {
				request.setUrl(request.getUrl().substring(0, request.getUrl().length() - 1));
			}
			exist.setUrl(request.getUrl());
		}
		if (null != request.getExternalSystemType()) {
			exist.setExternalSystemType(request.getExternalSystemType());
		}
		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(exist.getExternalSystemType());

		if (null != request.getProject()) {
			exist.setProject(request.getProject());
		}
		/* Hard referenced project update */
		exist.setProjectRef(projectName);
		if (null != request.getFields()) {
			exist.setFields(request.getFields());
		}

		/* Check input params for avoid external system duplication */
		if (!sysUrl.equalsIgnoreCase(exist.getUrl()) || !sysProject.equalsIgnoreCase(exist.getProject()) || !rpProject.equalsIgnoreCase(
				exist.getProjectRef())) {
			ExternalSystem duplicate = externalSystemRepository.findByUrlAndProject(exist.getUrl(), exist.getProject(),
					exist.getProjectRef()
			);
			expect(duplicate, isNull()).verify(EXTERNAL_SYSTEM_ALREADY_EXISTS, request.getUrl() + " & " + request.getProject());
		}

		AuthType auth;
		if (null != request.getExternalSystemAuth()) {
			auth = AuthType.findByName(request.getExternalSystemAuth());
			expect(auth, notNull()).verify(INCORRECT_AUTHENTICATION_TYPE, request.getExternalSystemAuth());
			exist.setExternalSystemAuth(auth);
			// Reset ext sys fields handler
			switch (auth) {
				case BASIC:
					if ((null != request.getUsername()) && (null != request.getPassword())) {
						exist = resetNTLMFields(exist);
						exist = resetOAuthFields(exist);
						exist.setUsername(request.getUsername());
						String encryptedPass = simpleEncryptor.encrypt(request.getPassword());
						exist.setPassword(encryptedPass);
					}
					break;
				case NTLM:
					if ((null != request.getUsername()) && (null != request.getPassword()) && (null != request.getDomain())) {
						exist = resetBasicFields(exist);
						exist = resetOAuthFields(exist);
						exist.setUsername(request.getUsername());
						String encryptedPass = simpleEncryptor.encrypt(request.getPassword());
						exist.setPassword(encryptedPass);
						exist.setDomain(request.getDomain());
					}
					break;
				case OAUTH:
					if (null != request.getAccessKey()) {
						exist = resetBasicFields(exist);
						exist = resetNTLMFields(exist);
						exist.setAccessKey(request.getAccessKey());
					}
					break;
				case APIKEY:
					if (null != request.getAccessKey()) {
						exist = resetBasicFields(exist);
						exist = resetNTLMFields(exist);
						exist.setAccessKey(request.getAccessKey());
					}
					break;
				default:
					//do nothing
			}

			if (auth.requiresPassword()) {
				String decrypted = exist.getPassword();
				exist.setPassword(simpleEncryptor.decrypt(exist.getPassword()));
				expect(externalSystemStrategy.connectionTest(exist), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
						projectName
				);
				exist.setPassword(decrypted);
			} else {
				expect(externalSystemStrategy.connectionTest(exist), equalTo(true)).verify(
						UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM, projectName);
			}
		}

		try {
			externalSystemRepository.save(exist);
		} catch (Exception e) {
			throw new ReportPortalException("Error during updating ExternalSystem", e);
		}

		eventPublisher.publishEvent(new ExternalSystemUpdatedEvent(exist, principalName));
		return new OperationCompletionRS("ExternalSystem with ID = '" + id + "' is successfully updated.");
	}

	@Override
	public OperationCompletionRS externalSystemConnect(String id, UpdateExternalSystemRQ updateRQ, String principalName) {
		ExternalSystem system = externalSystemRepository.findOne(id);
		expect(system, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, id);

		Project project = projectRepository.findByName(system.getProjectRef());
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, system.getProjectRef());
		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(updateRQ.getExternalSystemType());

		ExternalSystem details = new ExternalSystem();
		details.setUrl(updateRQ.getUrl());
		details.setProject(updateRQ.getProject());
		AuthType authType = AuthType.findByName(updateRQ.getExternalSystemAuth());
		expect(authType, notNull()).verify(INCORRECT_AUTHENTICATION_TYPE, updateRQ.getExternalSystemAuth());
		details.setExternalSystemAuth(authType);

		// noinspection ConstantConditions
		if (authType.requiresPassword()) {
			details.setUsername(updateRQ.getUsername());
			details.setPassword(updateRQ.getPassword());
			details.setDomain(updateRQ.getDomain());
			details.setAccessKey(updateRQ.getAccessKey());

		}
		expect(externalSystemStrategy.connectionTest(details), equalTo(true)).verify(UNABLE_INTERACT_WITH_EXTRERNAL_SYSTEM,
				system.getProjectRef()
		);

		return new OperationCompletionRS("Connection to ExternalSystem with ID = '" + id + "' is successfully performed.");
	}

	/**
	 * Reset BASIC authentication fields for external system entity
	 *
	 * @param input
	 * @return
	 */
	private static ExternalSystem resetBasicFields(ExternalSystem input) {
		input.setUsername(null);
		input.setPassword(null);
		return input;
	}

	/**
	 * Reset NTLM authentication fields for external system entity<br>
	 * <b>TFS specific</b>
	 *
	 * @param input
	 * @return
	 */
	private static ExternalSystem resetNTLMFields(ExternalSystem input) {
		input.setUsername(null);
		input.setPassword(null);
		input.setDomain(null);
		return input;
	}

	/**
	 * Reset OAuth authentication fields of external system entity
	 *
	 * @param input
	 * @return
	 */
	private static ExternalSystem resetOAuthFields(ExternalSystem input) {
		input.setAccessKey(null);
		return input;
	}
}
