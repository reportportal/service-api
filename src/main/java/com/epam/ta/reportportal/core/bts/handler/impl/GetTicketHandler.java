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
import com.epam.ta.reportportal.core.bts.handler.IGetTicketHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.store.commons.Predicates.not;
import static com.epam.ta.reportportal.ws.model.ErrorType.EXTERNAL_SYSTEM_NOT_FOUND;
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
		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		List<BugTrackingSystem> btsList = externalSystemRepository.findAllByProjectId(projectDetails.getProjectId());
		expect(btsList, not(CollectionUtils::isEmpty)).verify(PROJECT_NOT_CONFIGURED, projectName);
		BugTrackingSystem bugTrackingSystem = btsList.stream()
				.filter(it -> Objects.equals(it.getId(), systemId))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(EXTERNAL_SYSTEM_NOT_FOUND, systemId));
		//ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		//return externalSystemStrategy.getTicket(ticketId, system).orElse(null);
		//TODO after rewrite of bts
		return null;
	}

	@Override
	public List<PostFormField> getSubmitTicketFields(String ticketType, String projectName, Long systemId, ReportPortalUser user) {
		EntityUtils.takeProjectDetails(user, projectName);
		BugTrackingSystem system = validateExternalSystem(systemId);
		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		//		return externalSystemStrategy.getTicketFields(ticketType, system);
		//TODO after rewrite of bts
		return null;
	}

	@Override
	public List<String> getAllowableIssueTypes(String projectName, Long systemId, ReportPortalUser user) {
		EntityUtils.takeProjectDetails(user, projectName);
		BugTrackingSystem system = validateExternalSystem(systemId);
		//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		//		return externalSystemStrategy.getIssueTypes(system);
		//TODO after rewrite of bts
		return null;
	}

	private BugTrackingSystem validateExternalSystem(Long systemId) {
		return externalSystemRepository.findById(systemId)
				.orElseThrow(() -> new ReportPortalException(EXTERNAL_SYSTEM_NOT_FOUND, systemId));
	}
}
