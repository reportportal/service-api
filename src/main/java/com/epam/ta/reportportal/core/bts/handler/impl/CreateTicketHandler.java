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
import com.epam.ta.reportportal.core.bts.handler.ICreateTicketHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.BugTrackingSystemRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.bts.BugTrackingSystem;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link ICreateTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateTicketHandler implements ICreateTicketHandler {

//	@Autowired
//	private StrategyProvider strategyProvider;
//
//	@Autowired
//	private ProjectRepository projectRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private BugTrackingSystemRepository bugTrackingSystemRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public Ticket createIssue(PostTicketRQ postTicketRQ, String projectName, Long systemId, ReportPortalUser user) {
		validatePostTicketRQ(postTicketRQ);
		List<TestItem> testItems = testItemRepository.findAllById(postTicketRQ.getBackLinks().keySet());
		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);
		BugTrackingSystem bugTrackingSystem = bugTrackingSystemRepository.findById(systemId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));

		expect(bugTrackingSystem.getDefectFormFields(), notNull()).verify(BAD_REQUEST_ERROR, "There aren't any submitted BTS fields!");
//		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
//		Ticket ticket = externalSystemStrategy.submitTicket(postTicketRQ, system);
//		testItems.forEach(
//				item -> eventPublisher.publishEvent(new TicketPostedEvent(ticket, item.getId(), username, projectName, item.getName())));
		return null;
	}

	/**
	 * Additional validations to {@link PostTicketRQ}.
	 *
	 * @param postTicketRQ
	 */
	private void validatePostTicketRQ(PostTicketRQ postTicketRQ) {
		if (postTicketRQ.getIsIncludeLogs() || postTicketRQ.getIsIncludeScreenshots()) {
			expect(postTicketRQ.getBackLinks(), notNull()).verify(UNABLE_POST_TICKET,
					"Test item id should be specified, when logs required in ticket description."
			);
		}
	}
}