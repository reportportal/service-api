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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.NotificationRuleCreatedEvent;
import com.epam.ta.reportportal.core.project.validator.notification.ProjectNotificationValidator;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.enums.LogicalOperator;
import com.epam.ta.reportportal.entity.enums.SendCase;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.LaunchAttributeRule;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.ta.reportportal.model.project.ProjectConfiguration;
import com.epam.ta.reportportal.model.project.ProjectResource;
import com.epam.ta.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.ta.reportportal.model.project.email.SenderCaseDTO;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateProjectNotificationHandlerImplTest {

  private static final long DEFAULT_PROJECT_ID = 1L;
  private static final String DEFAULT_RULE_NAME = "Rule1";

  private static final String RULE_TYPE = "email";

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final MessageBus messageBus = mock(MessageBus.class);
  private final ProjectConverter projectConverter = new ProjectConverter();
  private final ProjectNotificationValidator projectNotificationValidator =
      new ProjectNotificationValidator(senderCaseRepository);

  private final CreateProjectNotificationHandlerImpl service =
      new CreateProjectNotificationHandlerImpl(senderCaseRepository, messageBus, projectConverter,
          projectNotificationValidator
      );

  @Captor
  private ArgumentCaptor<ActivityEvent> activityCaptor;

  private SenderCaseDTO createNotificationRQ;
  private Project project;
  private ReportPortalUser rpUser;

  @BeforeEach
  public void beforeEach() {
    createNotificationRQ = new SenderCaseDTO();
    createNotificationRQ.setSendCase("always");
    createNotificationRQ.setType("email");
    createNotificationRQ.setRuleName(DEFAULT_RULE_NAME);
    createNotificationRQ.setAttributesOperator(LogicalOperator.AND.getOperator());
    createNotificationRQ.setRecipients(Collections.singletonList("OWNER"));
    createNotificationRQ.setLaunchNames(Collections.singletonList("test launch"));
    createNotificationRQ.setEnabled(true);
    ItemAttributeResource launchAttribute = new ItemAttributeResource();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    createNotificationRQ.setAttributes(Sets.newHashSet(launchAttribute));

    project = new Project();
    project.setId(DEFAULT_PROJECT_ID);

    rpUser = mock(ReportPortalUser.class);
  }

  @Test
  public void createNotificationWithExistingRuleNameTest() {
    SenderCase existingSenderCase = mock(SenderCase.class);

    when(senderCaseRepository.findByProjectIdAndTypeAndRuleNameIgnoreCase(DEFAULT_PROJECT_ID,
        RULE_TYPE,
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
    SenderCase dupeCase = new SenderCase();
    dupeCase.setSendCase(SendCase.ALWAYS);
    dupeCase.setType("email");
    dupeCase.setRuleName("Rule2");
    dupeCase.setAttributesOperator(LogicalOperator.AND);
    dupeCase.setRecipients(Collections.singleton("OWNER"));
    dupeCase.setLaunchNames(Collections.singleton("test launch"));
    dupeCase.setEnabled(true);
    dupeCase.setProject(project);
    LaunchAttributeRule launchAttribute = new LaunchAttributeRule();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    dupeCase.setLaunchAttributeRules(Collections.singleton(launchAttribute));

    when(senderCaseRepository.findAllByProjectId(DEFAULT_PROJECT_ID)).thenReturn(
        Collections.singletonList(dupeCase));

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.createNotification(project, createNotificationRQ, rpUser)
    ).getMessage().contains("Project notification settings contain duplicate cases for this communication channel"));
  }

  @Test
  void createNotificationWhenRuleCreatedShouldPublishNotificationRuleCreatedEvent() {
    // given
    CreateProjectNotificationHandlerImpl serviceWithRealConverter = new CreateProjectNotificationHandlerImpl(
        senderCaseRepository, messageBus, projectConverter, projectNotificationValidator);
    project.setId(7L);
    project.setOrganizationId(77L);

    ProjectResource pr = new ProjectResource();
    pr.setProjectId(7L);
    ProjectConfiguration cfg = new ProjectConfiguration();
    ProjectNotificationConfigDTO pcfg = new ProjectNotificationConfigDTO();
    pcfg.setSenderCases(new java.util.ArrayList<>());
    cfg.setProjectConfig(pcfg);
    pr.setConfiguration(cfg);
    projectConverter.TO_PROJECT_RESOURCE = p -> pr;

    when(senderCaseRepository.findByProjectIdAndTypeAndRuleNameIgnoreCase(7L, createNotificationRQ.getType(),
        createNotificationRQ.getRuleName())).thenReturn(java.util.Optional.empty());
    when(senderCaseRepository.findAllByProjectId(7L)).thenReturn(java.util.Collections.emptyList());

    SenderCase saved = new SenderCase();
    saved.setId(55L);
    saved.setProject(new Project());
    saved.getProject().setId(7L);
    saved.getProject().setOrganizationId(77L);
    saved.setRuleName(createNotificationRQ.getRuleName());
    when(senderCaseRepository.save(any(SenderCase.class))).thenReturn(saved);

    when(rpUser.getUserId()).thenReturn(5L);
    when(rpUser.getUsername()).thenReturn("u1");

    // when
    serviceWithRealConverter.createNotification(project, createNotificationRQ, rpUser);
    // then
    verify(messageBus).publishActivity(activityCaptor.capture());
    var activityCaptorValue = activityCaptor.getValue();
    assertInstanceOf(NotificationRuleCreatedEvent.class, activityCaptorValue);
    var activity = activityCaptorValue.toActivity();
    assertEquals("createNotificationRule", activity.getEventName());
    assertEquals(7L, activity.getProjectId());
    assertEquals(77L, activity.getOrganizationId());
  }

}
