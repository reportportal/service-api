/*
 * Copyright 2019 EPAM Systems
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
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.bts.handler.CreateTicketHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.TicketPostedEvent;
import com.epam.ta.reportportal.core.integration.GetIntegrationHandler;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_POST_TICKET;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link CreateTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class CreateTicketHandlerImpl implements CreateTicketHandler {

	private final TestItemRepository testItemRepository;
	private final MessageBus messageBus;
	private final PluginBox pluginBox;
	private final GetIntegrationHandler getIntegrationHandler;

	@Autowired
	public CreateTicketHandlerImpl(TestItemRepository testItemRepository, PluginBox pluginBox, MessageBus messageBus,
			GetIntegrationHandler getIntegrationHandler) {
		this.testItemRepository = testItemRepository;
		this.pluginBox = pluginBox;
		this.messageBus = messageBus;
		this.getIntegrationHandler = getIntegrationHandler;
	}

	@Override
	public Ticket createIssue(PostTicketRQ postTicketRQ, Long integrationId, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user) {
		validatePostTicketRQ(postTicketRQ);

		List<TestItem> testItems = ofNullable(postTicketRQ.getBackLinks()).map(links -> testItemRepository.findAllById(links.keySet()))
				.orElseGet(Collections::emptyList);
		List<TestItemActivityResource> before = testItems.stream()
				.map(it -> TO_ACTIVITY_RESOURCE.apply(it, projectDetails.getProjectId()))
				.collect(Collectors.toList());

		Integration integration = getIntegrationHandler.getEnabledBtsIntegration(projectDetails, integrationId);

		expect(BtsConstants.DEFECT_FORM_FIELDS.getParam(integration.getParams()), notNull()).verify(BAD_REQUEST_ERROR,
				"There aren't any submitted BTS fields!"
		);

		BtsExtension btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR,
						Suppliers.formattedSupplier("BugTracking plugin for {} isn't installed",
								BtsConstants.PROJECT.getParam(integration.getParams())
						).get()
				));

		Ticket ticket = btsExtension.submitTicket(postTicketRQ, integration);

		before.forEach(it -> messageBus.publishActivity(new TicketPostedEvent(ticket, user.getUserId(), user.getUsername(), it)));
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