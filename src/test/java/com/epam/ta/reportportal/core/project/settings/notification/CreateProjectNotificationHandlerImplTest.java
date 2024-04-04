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

import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.rules.exception.ErrorType.RESOURCE_ALREADY_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.project.validator.notification.ProjectNotificationValidator;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
class CreateProjectNotificationHandlerImplTest {

  private static final long DEFAULT_PROJECT_ID = 1L;
  private static final String DEFAULT_RULE_NAME = "Rule1";

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final MessageBus messageBus = mock(MessageBus.class);
  private final ProjectConverter projectConverter = mock(ProjectConverter.class);
  private final ProjectNotificationValidator projectNotificationValidator =
      new ProjectNotificationValidator(senderCaseRepository);

  private final CreateProjectNotificationHandlerImpl service =
      new CreateProjectNotificationHandlerImpl(senderCaseRepository, messageBus, projectConverter,
          projectNotificationValidator
      );

  private SenderCaseDTO createNotificationRQ;
  private Project project;
  private ReportPortalUser rpUser;

  @BeforeEach
  public void beforeEach() {
    createNotificationRQ = new SenderCaseDTO();
    createNotificationRQ.setSendCase("always");
    createNotificationRQ.setRuleName(DEFAULT_RULE_NAME);
    createNotificationRQ.setAttributesOperator(LogicalOperator.AND.getOperator());
    createNotificationRQ.setRecipients(Collections.singletonList("OWNER"));
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

    when(senderCaseRepository.findByProjectIdAndRuleNameIgnoreCase(DEFAULT_PROJECT_ID,
        DEFAULT_RULE_NAME
    )).thenReturn(Optional.of(existingSenderCase));

    assertEquals(assertThrows(ReportPortalException.class,
        () -> service.createNotification(project, createNotificationRQ, rpUser)
    ).getMessage(), formattedSupplier(RESOURCE_ALREADY_EXISTS.getDescription(),
        createNotificationRQ.getRuleName()
    ).get());
  }

  @Test
  public void createNotificationWithNonExistingSendCaseTest() {
    createNotificationRQ.setSendCase("NonExistingSendCase");

    assertEquals(assertThrows(ReportPortalException.class,
        () -> service.createNotification(project, createNotificationRQ, rpUser)
    ).getMessage(), formattedSupplier(BAD_REQUEST_ERROR.getDescription(),
        createNotificationRQ.getSendCase()
    ).get());
  }

  @Test
  public void createNotificationWithNullOrEmptyRecipientsTest() {
    createNotificationRQ.setRecipients(null);

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.createNotification(project, createNotificationRQ, rpUser)
    ).getMessage().contains("Recipients list should not be null"));

    createNotificationRQ.setRecipients(Collections.emptyList());

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.createNotification(project, createNotificationRQ, rpUser)
    ).getMessage().contains("Empty recipients list for email case"));
  }

  @Test
  public void createNotificationWithDuplicateContentButWithDifferentRuleNameTest() {
    SenderCase dupeCreateNotificationRQ = mock(SenderCase.class);
    when(dupeCreateNotificationRQ.getSendCase()).thenReturn(SendCase.ALWAYS);
    when(dupeCreateNotificationRQ.getRuleName()).thenReturn("Rule2");
    when(dupeCreateNotificationRQ.getAttributesOperator()).thenReturn(LogicalOperator.AND);
    when(dupeCreateNotificationRQ.getRecipients()).thenReturn(Collections.singleton("OWNER"));
    when(dupeCreateNotificationRQ.getLaunchNames()).thenReturn(
        Collections.singleton("test launch"));
    when(dupeCreateNotificationRQ.isEnabled()).thenReturn(true);
    when(dupeCreateNotificationRQ.getProject()).thenReturn(project);

    LaunchAttributeRule launchAttribute = mock(LaunchAttributeRule.class);
    when(launchAttribute.getKey()).thenReturn("key");
    when(launchAttribute.getValue()).thenReturn("val");
    when(dupeCreateNotificationRQ.getLaunchAttributeRules()).thenReturn(
        Collections.singleton(launchAttribute));

    when(senderCaseRepository.findAllByProjectId(DEFAULT_PROJECT_ID)).thenReturn(
        Collections.singletonList(dupeCreateNotificationRQ));

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.createNotification(project, createNotificationRQ, rpUser)
    ).getMessage().contains("Project email settings contain duplicate cases"));
  }

}
