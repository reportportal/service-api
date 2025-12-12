/*
 *
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
 *
 */

package com.epam.reportportal.core.project.settings.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.events.activity.converter.NotificationRuleUpdatedEventConverter;
import com.epam.reportportal.core.events.domain.NotificationRuleUpdatedEvent;
import com.epam.reportportal.core.project.validator.notification.ProjectNotificationValidator;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.SenderCaseRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.LogicalOperator;
import com.epam.reportportal.infrastructure.persistence.entity.enums.SendCase;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.email.LaunchAttributeRule;
import com.epam.reportportal.infrastructure.persistence.entity.project.email.SenderCase;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.project.ProjectConfiguration;
import com.epam.reportportal.model.project.ProjectResource;
import com.epam.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.reportportal.model.project.email.SenderCaseDTO;
import com.epam.reportportal.reporting.ItemAttributeResource;
import com.epam.reportportal.ws.converter.converters.ProjectConverter;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
@ExtendWith(MockitoExtension.class)
class UpdateProjectNotificationHandlerImplTest {

  private static final long DEFAULT_PROJECT_ID = 1L;
  private static final String DEFAULT_RULE_NAME = "Rule1";

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
  private final ProjectConverter projectConverter = new ProjectConverter();
  private final ProjectNotificationValidator projectNotificationValidator =
      new ProjectNotificationValidator(senderCaseRepository);

  private final UpdateProjectNotificationHandlerImpl service =
      new UpdateProjectNotificationHandlerImpl(senderCaseRepository, eventPublisher,
          projectConverter,
          projectNotificationValidator
      );

  @Captor
  private ArgumentCaptor<NotificationRuleUpdatedEvent> activityCaptor;

  private SenderCaseDTO updateNotificationRQ;
  private Project project;
  private ReportPortalUser rpUser;

  @BeforeEach
  public void beforeEach() {
    updateNotificationRQ = new SenderCaseDTO();
    updateNotificationRQ.setId(1L);
    updateNotificationRQ.setSendCase("always");
    updateNotificationRQ.setAttributesOperator(LogicalOperator.AND.getOperator());
    updateNotificationRQ.setRuleName(DEFAULT_RULE_NAME);
    updateNotificationRQ.setType("email");
    updateNotificationRQ.setRecipients(Collections.singletonList("OWNER"));
    updateNotificationRQ.setLaunchNames(Collections.singletonList("test launch"));
    updateNotificationRQ.setEnabled(true);
    ItemAttributeResource launchAttribute = new ItemAttributeResource();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    updateNotificationRQ.setAttributes(Sets.newHashSet(launchAttribute));

    project = new Project();
    project.setId(DEFAULT_PROJECT_ID);

    rpUser = mock(ReportPortalUser.class);
  }

  @Test
  public void updateNonExistingNotificationTest() {
    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains("Did you use correct Notification ID?"));
  }

  @Test
  public void updateNotificationButWithDifferentProjectTest() {
    SenderCase sc = mock(SenderCase.class);
    Project scProject = mock(Project.class);

    when(scProject.getId()).thenReturn(2L);
    when(sc.getProject()).thenReturn(scProject);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains("Did you use correct Notification ID?"));
  }

  @Test
  public void updateNotificationWithNonExistingSendCaseTest() {
    SenderCase sc = new SenderCase();
    Project project = new Project();
    project.setId(1L);
    sc.setProject(project);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    updateNotificationRQ.setSendCase("NonExistingSendCase");
    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains(updateNotificationRQ.getSendCase()));
  }

  @Test
  public void updateNotificationWithNullOrEmptyRecipientsTest() {
    SenderCase sc = new SenderCase();
    Project project = new Project();
    project.setId(DEFAULT_PROJECT_ID);
    sc.setProject(project);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    updateNotificationRQ.setRecipients(null);
    String s = assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage();
    assertTrue(s.contains("Recipients list should not be null"));

    updateNotificationRQ.setRecipients(Collections.emptyList());

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains("Empty recipients list for email case"));
  }

  @Test
  public void updateNotificationWithDuplicateContentButWithDifferentRuleNameTest() {
    SenderCase sc = new SenderCase();
    Project project = new Project();
    project.setId(DEFAULT_PROJECT_ID);
    sc.setProject(project);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    SenderCase modelForUpdate = new SenderCase();
    modelForUpdate.setId(1L);
    modelForUpdate.setSendCase(SendCase.ALWAYS);
    modelForUpdate.setRuleName("Rule2");
    modelForUpdate.setType("email");
    modelForUpdate.setAttributesOperator(LogicalOperator.AND);
    modelForUpdate.setRecipients(Collections.singleton("OWNER"));
    modelForUpdate.setLaunchNames(Collections.singleton("test launch1"));
    modelForUpdate.setEnabled(true);
    modelForUpdate.setProject(project);

    SenderCase dupeUpdateNotification = new SenderCase();
    dupeUpdateNotification.setId(2L);
    dupeUpdateNotification.setSendCase(SendCase.ALWAYS);
    dupeUpdateNotification.setRuleName("Rule3");
    dupeUpdateNotification.setType("email");
    dupeUpdateNotification.setAttributesOperator(LogicalOperator.AND);
    dupeUpdateNotification.setRecipients(Collections.singleton("OWNER"));
    dupeUpdateNotification.setLaunchNames(Collections.singleton("test launch"));
    dupeUpdateNotification.setEnabled(true);
    dupeUpdateNotification.setProject(project);

    LaunchAttributeRule launchAttribute = new LaunchAttributeRule();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    dupeUpdateNotification.setLaunchAttributeRules(Collections.singleton(launchAttribute));

    when(senderCaseRepository.findAllByProjectId(DEFAULT_PROJECT_ID)).thenReturn(
        List.of(modelForUpdate, dupeUpdateNotification));

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains(
        "Project notification settings contain duplicate cases for this communication channel"));
  }

  @Test
  void updateNotificationWhenRuleUpdatedShouldPublishNotificationRuleUpdatedEvent() {
    // given
    UpdateProjectNotificationHandlerImpl serviceReal = new UpdateProjectNotificationHandlerImpl(
        senderCaseRepository,
        eventPublisher, projectConverter,
        projectNotificationValidator);

    Project project = new Project();
    project.setId(7L);
    project.setOrganizationId(77L);

    ProjectResource pr = new ProjectResource();
    pr.setProjectId(project.getId());
    ProjectConfiguration cfg = new ProjectConfiguration();
    ProjectNotificationConfigDTO pcfg = new ProjectNotificationConfigDTO();
    pcfg.setSenderCases(new java.util.ArrayList<>());
    cfg.setProjectConfig(pcfg);
    pr.setConfiguration(cfg);
    projectConverter.TO_PROJECT_RESOURCE = p -> pr;

    SenderCase existing = new SenderCase();
    existing.setId(55L);
    existing.setProject(project);
    existing.setRuleName("rule-X");
    existing.setAttributesOperator(LogicalOperator.AND);
    existing.setSendCase(SendCase.ALWAYS);
    when(senderCaseRepository.findById(55L)).thenReturn(java.util.Optional.of(existing));

    SenderCase saved = new SenderCase();
    saved.setId(55L);
    saved.setProject(project);
    saved.setRuleName("rule-X");
    saved.setAttributesOperator(LogicalOperator.AND);
    saved.setSendCase(SendCase.ALWAYS);
    when(senderCaseRepository.save(any(SenderCase.class))).thenReturn(saved);

    SenderCaseDTO rq = new SenderCaseDTO();
    rq.setId(55L);
    rq.setType("email");
    rq.setRuleName("rule-X");
    rq.setEnabled(true);
    rq.setSendCase("always");
    rq.setRecipients(java.util.List.of("user@example.com"));
    rq.setAttributesOperator(LogicalOperator.AND.getOperator());

    when(rpUser.getUserId()).thenReturn(5L);
    when(rpUser.getUsername()).thenReturn("u1");

    // when
    serviceReal.updateNotification(project, rq, rpUser);

    // then
    verify(eventPublisher).publishEvent(activityCaptor.capture());
    var activityCaptorValue = activityCaptor.getValue();
    assertInstanceOf(NotificationRuleUpdatedEvent.class, activityCaptorValue);
    NotificationRuleUpdatedEventConverter converter = new NotificationRuleUpdatedEventConverter();
    var activity = converter.convert((NotificationRuleUpdatedEvent) activityCaptorValue);
    assertEquals("updateNotificationRule", activity.getEventName());
    assertEquals(project.getId(), activity.getProjectId());
    assertEquals(77L, activity.getOrganizationId());
  }

}
