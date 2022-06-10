/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.project.settings.notification;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.project.email.SenderCaseDTO;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.RESOURCE_ALREADY_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
class CreateProjectNotificationHandlerImplTest {
	private static final long DEFAULT_PROJECT_ID = 1L;
	private static final String DEFAULT_RULE_NAME = "Rule1";

	private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
	private final MessageBus messageBus = mock(MessageBus.class);
	private final ProjectConverter projectConverter = mock(ProjectConverter.class);
	private final CreateProjectNotificationHandlerImpl service = new CreateProjectNotificationHandlerImpl(senderCaseRepository, messageBus,
			projectConverter);

	private SenderCaseDTO createNotificationRQ;
	private Project project;
	private ReportPortalUser rpUser;

	@BeforeEach
	public void beforeEach() {
		createNotificationRQ = new SenderCaseDTO();
		createNotificationRQ.setSendCase("always");
		createNotificationRQ.setRuleName(DEFAULT_RULE_NAME);
		createNotificationRQ.setRecipients(Collections.singletonList("default"));
		createNotificationRQ.setLaunchNames(Collections.singletonList("test launch"));
		createNotificationRQ.setEnabled(true);
		ItemAttributeResource launchAttribute = new ItemAttributeResource();
		launchAttribute.setKey("key");
		launchAttribute.setValue("val");
		createNotificationRQ.setAttributes(Sets.newHashSet(launchAttribute));

		project = mock(Project.class);
		when(project.getId()).thenReturn(DEFAULT_PROJECT_ID);

		rpUser = mock(ReportPortalUser.class);
	}

	@Test
	public void createNotificationWithExistingRuleNameTest() {
		SenderCase existingSenderCase = mock(SenderCase.class);

		when(senderCaseRepository.findByProjectIdAndRuleNameIgnoreCase(DEFAULT_PROJECT_ID, DEFAULT_RULE_NAME))
				.thenReturn(Optional.of(existingSenderCase));

		assertThrows(ReportPortalException.class, () -> service.createNotification(project, createNotificationRQ, rpUser),
				formattedSupplier(RESOURCE_ALREADY_EXISTS.getDescription(), createNotificationRQ.getRuleName()).get());
	}

	@Test
	public void createNotificationWithNonExistingSendCaseTest() {
		createNotificationRQ.setSendCase("NonExistingSendCase");

		assertThrows(ReportPortalException.class, () -> service.createNotification(project, createNotificationRQ, rpUser),
				formattedSupplier(BAD_REQUEST_ERROR.getDescription(), createNotificationRQ.getSendCase()).get());
	}

	@Test
	public void createNotificationWithNullOrEmptyRecipientsTest() {
		createNotificationRQ.setRecipients(null);

		assertThrows(ReportPortalException.class, () -> service.createNotification(project, createNotificationRQ, rpUser),
				"Recipients list should not be null");

		createNotificationRQ.setRecipients(Collections.emptyList());

		assertThrows(ReportPortalException.class, () -> service.createNotification(project, createNotificationRQ, rpUser),
				formattedSupplier(BAD_REQUEST_ERROR.getDescription(),
						formattedSupplier("Empty recipients list for email case '{}' ", createNotificationRQ)));
	}

	@Test
	public void createNotificationWithDuplicateContentButWithDifferentRuleNameTest() {
		SenderCase dupeCreateNotificationRQ = mock(SenderCase.class);
		when(dupeCreateNotificationRQ.getSendCase()).thenReturn(SendCase.ALWAYS);
		when(dupeCreateNotificationRQ.getRuleName()).thenReturn("Rule2");
		when(dupeCreateNotificationRQ.getRecipients()).thenReturn(Collections.singleton("default"));
		when(dupeCreateNotificationRQ.getLaunchNames()).thenReturn(Collections.singleton("test launch"));
		when(dupeCreateNotificationRQ.isEnabled()).thenReturn(true);
		when(dupeCreateNotificationRQ.getProject()).thenReturn(project);

		LaunchAttributeRule launchAttribute = mock(LaunchAttributeRule.class);
		when(launchAttribute.getKey()).thenReturn("key");
		when(launchAttribute.getValue()).thenReturn("val");
		when(dupeCreateNotificationRQ.getLaunchAttributeRules()).thenReturn(Collections.singleton(launchAttribute));

		when(senderCaseRepository.findAllByProjectId(DEFAULT_PROJECT_ID)).thenReturn(Collections.singletonList(dupeCreateNotificationRQ));

		assertThrows(ReportPortalException.class, () -> service.createNotification(project, createNotificationRQ, rpUser),
				formattedSupplier(BAD_REQUEST_ERROR.getDescription(), "Project email settings contain duplicate cases"));
	}

}
