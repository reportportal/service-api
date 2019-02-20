/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.bts.handler.impl;

import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.CreateTicketHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TicketPostedEvent;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;

/**
 * Default implementation of {@link CreateTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateTicketHandlerImpl implements CreateTicketHandler {

	private final TestItemRepository testItemRepository;

	private final IntegrationRepository integrationRepository;

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	private MessageBus messageBus;

	private final PluginBox pluginBox;

	@Autowired
	public CreateTicketHandlerImpl(TestItemRepository testItemRepository, IntegrationRepository integrationRepository,
			ApplicationEventPublisher eventPublisher, PluginBox pluginBox) {
		this.testItemRepository = testItemRepository;
		this.integrationRepository = integrationRepository;
		this.eventPublisher = eventPublisher;
		this.pluginBox = pluginBox;
	}

	@Override
	public Ticket createIssue(PostTicketRQ postTicketRQ, Long integrationId, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		validatePostTicketRQ(postTicketRQ);
		List<TestItem> testItems = testItemRepository.findAllById(postTicketRQ.getBackLinks().keySet());
		List<TestItemActivityResource> before = testItems.stream().map(TO_ACTIVITY_RESOURCE).collect(Collectors.toList());

		Integration integration = integrationRepository.findById(integrationId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, integrationId));

		expect(BtsConstants.DEFECT_FORM_FIELDS.getParam(integration.getParams()), notNull()).verify(BAD_REQUEST_ERROR,
				"There aren't any submitted BTS fields!"
		);
		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				BtsConstants.PROJECT.getParam(integration.getParams())
		);

		Ticket ticket = btsExtension.get().submitTicket(postTicketRQ, integration);
		before.forEach(it -> messageBus.publishActivity(new TicketPostedEvent(ticket, user.getUserId(), it)));
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