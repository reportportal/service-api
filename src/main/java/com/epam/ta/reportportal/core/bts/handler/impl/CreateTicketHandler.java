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

import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.ICreateTicketHandler;
import com.epam.ta.reportportal.core.events.activity.TicketPostedEvent;
import com.epam.ta.reportportal.core.plugin.PluginBox;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.BtsConstants;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

	private final ProjectRepository projectRepository;

	private final TestItemRepository testItemRepository;

	private final IntegrationRepository integrationRepository;

	private final ApplicationEventPublisher eventPublisher;

	private final PluginBox pluginBox;

	@Autowired
	public CreateTicketHandler(ProjectRepository projectRepository, TestItemRepository testItemRepository,
			IntegrationRepository integrationRepository, ApplicationEventPublisher eventPublisher, PluginBox pluginBox) {
		this.projectRepository = projectRepository;
		this.testItemRepository = testItemRepository;
		this.integrationRepository = integrationRepository;
		this.eventPublisher = eventPublisher;
		this.pluginBox = pluginBox;
	}

	@Override
	public Ticket createIssue(PostTicketRQ postTicketRQ, String projectName, Long systemId, ReportPortalUser user) {
		validatePostTicketRQ(postTicketRQ);

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		List<TestItem> testItems = testItemRepository.findAllById(postTicketRQ.getBackLinks().keySet());
		//		ReportPortalUser.ProjectDetails projectDetails = ProjectUtils.extractProjectDetails(user, projectName);

		Integration integration = integrationRepository.findById(systemId)
				.orElseThrow(() -> new ReportPortalException(INTEGRATION_NOT_FOUND, systemId));

		expect(BtsConstants.DEFECT_FORM_FIELDS.getParam(integration.getParams()), notNull()).verify(BAD_REQUEST_ERROR,
				"There aren't any submitted BTS fields!"
		);
		Optional<BtsExtension> btsExtension = pluginBox.getInstance(integration.getType().getName(), BtsExtension.class);
		expect(btsExtension, Optional::isPresent).verify(BAD_REQUEST_ERROR,
				"BugTracking plugin for {} isn't installed",
				BtsConstants.PROJECT.getParam(integration.getParams())
		);

		Ticket ticket = btsExtension.get().submitTicket(postTicketRQ, integration);
		testItems.forEach(item -> eventPublisher.publishEvent(new TicketPostedEvent(ticket,
				item,
				user.getUserId(),
				project.getId(),
				item.getName()
		)));
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