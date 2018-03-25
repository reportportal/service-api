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
import com.epam.ta.reportportal.core.externalsystem.handler.ICreateTicketHandler;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.events.TicketPostedEvent;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link ICreateTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateTicketHandler implements ICreateTicketHandler {

	@Autowired
	private StrategyProvider strategyProvider;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ExternalSystemRepository externalSystemRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public Ticket createIssue(PostTicketRQ postTicketRQ, String projectName, String systemId, String username) {
		validatePostTicketRQ(postTicketRQ);
		List<TestItem> testItems = testItemRepository.findByIds(
				postTicketRQ.getBackLinks().keySet(), ImmutableList.<String>builder().add("_id").add("name").build());
		Project project = projectRepository.findByName(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);
		List<String> ids = project.getConfiguration().getExternalSystem();
		expect(ids, notNull()).verify(PROJECT_NOT_CONFIGURED, projectName);
		ExternalSystem system = externalSystemRepository.findOne(systemId);
		expect(system, notNull()).verify(EXTERNAL_SYSTEM_NOT_FOUND, systemId);
		expect(system.getFields(), notNull()).verify(BAD_REQUEST_ERROR, "There aren't any submitted BTS fields!");
		ExternalSystemStrategy externalSystemStrategy = strategyProvider.getStrategy(system.getExternalSystemType());
		Ticket ticket = externalSystemStrategy.submitTicket(postTicketRQ, system);
		testItems.forEach(
				item -> eventPublisher.publishEvent(new TicketPostedEvent(ticket, item.getId(), username, projectName, item.getName())));
		return ticket;
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