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
import com.epam.ta.reportportal.core.bts.handler.IGetTicketHandler;
import com.epam.ta.reportportal.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.INTEGRATION_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_CONFIGURED;

/**
 * Default implementation of {@link IGetTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetTicketHandler implements IGetTicketHandler {

	private final BugTrackingSystemRepository externalSystemRepository;

	@Autowired
	public GetTicketHandler(BugTrackingSystemRepository externalSystemRepository) {
		this.externalSystemRepository = externalSystemRepository;
	}

	@Override
	public Ticket getTicket(String ticketId, String projectName, Long systemId, ReportPortalUser user) {
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		List<BugTrackingSystem> btsList = externalSystemRepository.findAllByProjectId(projectDetails.getProjectId());
		expect(btsList, not(CollectionUtils::isEmpty)).verify(PROJECT_NOT_CONFIGURED, projectName);
		BugTrackingSystem bugTrackingSystem = btsList.stream()
				.filter(it -> Objects.equals(it.getId(), systemId))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));
		//ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		//return externalSystemStrategy.getTicket(ticketId, system).orElse(null);
		//TODO after rewrite of bts
		return null;
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, String projectName, Long systemId, ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		BugTrackingSystem system = validateExternalSystem(systemId);
		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		//		return externalSystemStrategy.getTicketFields(ticketType, system);
		//TODO after rewrite of bts
		return null;
	}

	@Override
	public List<String> getAllowableIssueTypes(String projectName, Long systemId, ReportPortalUser user) {
		ProjectUtils.extractProjectDetails(user, projectName);
		BugTrackingSystem system = validateExternalSystem(systemId);
		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		//		return externalSystemStrategy.getIssueTypes(system);
		//TODO after rewrite of bts
		return null;
	}

	private BugTrackingSystem validateExternalSystem(Long systemId) {
		return externalSystemRepository.findById(systemId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));
	}
}
