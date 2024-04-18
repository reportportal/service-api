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

package com.epam.ta.reportportal.core.project.settings.notification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
class UpdateProjectNotificationHandlerImplTest {

  private static final long DEFAULT_PROJECT_ID = 1L;
  private static final String DEFAULT_RULE_NAME = "Rule1";

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final MessageBus messageBus = mock(MessageBus.class);
  private final ProjectConverter projectConverter = mock(ProjectConverter.class);
  private final ProjectNotificationValidator projectNotificationValidator =
      new ProjectNotificationValidator(senderCaseRepository);

  private final UpdateProjectNotificationHandlerImpl service =
      new UpdateProjectNotificationHandlerImpl(senderCaseRepository, messageBus, projectConverter,
          projectNotificationValidator
      );

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
    updateNotificationRQ.setRecipients(Collections.singletonList("OWNER"));
    updateNotificationRQ.setLaunchNames(Collections.singletonList("test launch"));
    updateNotificationRQ.setEnabled(true);
    ItemAttributeResource launchAttribute = new ItemAttributeResource();
    launchAttribute.setKey("key");
    launchAttribute.setValue("val");
    updateNotificationRQ.setAttributes(Sets.newHashSet(launchAttribute));

    project = mock(Project.class);
    when(project.getId()).thenReturn(DEFAULT_PROJECT_ID);

    rpUser = mock(ReportPortalUser.class);
  }

  @Test
  public void updateNonExistingNotificationTest() {
    Assertions.assertTrue(assertThrows(ReportPortalException.class,
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

    Assertions.assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains("Did you use correct Notification ID?"));
  }

  @Test
  public void updateNotificationWithNonExistingSendCaseTest() {
    SenderCase sc = mock(SenderCase.class);
    Project project = mock(Project.class);

    when(project.getId()).thenReturn(1L);
    when(sc.getProject()).thenReturn(project);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    updateNotificationRQ.setSendCase("NonExistingSendCase");
    Assertions.assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains(updateNotificationRQ.getSendCase()));
  }

  @Test
  public void updateNotificationWithNullOrEmptyRecipientsTest() {
    SenderCase sc = mock(SenderCase.class);
    Project project = mock(Project.class);

    when(project.getId()).thenReturn(DEFAULT_PROJECT_ID);
    when(sc.getProject()).thenReturn(project);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    updateNotificationRQ.setRecipients(null);
    String s = assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage();
    Assertions.assertTrue(s.contains("Recipients list should not be null"));

    updateNotificationRQ.setRecipients(Collections.emptyList());

    Assertions.assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains("Empty recipients list for email case"));
  }

  @Test
  public void updateNotificationWithDuplicateContentButWithDifferentRuleNameTest() {
    SenderCase sc = mock(SenderCase.class);
    Project project = mock(Project.class);

    when(project.getId()).thenReturn(DEFAULT_PROJECT_ID);
    when(sc.getProject()).thenReturn(project);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    SenderCase modelForUpdate = mock(SenderCase.class);
    when(modelForUpdate.getId()).thenReturn(1L);
    when(modelForUpdate.getSendCase()).thenReturn(SendCase.ALWAYS);
    when(modelForUpdate.getRuleName()).thenReturn("Rule2");
    when(modelForUpdate.getAttributesOperator()).thenReturn(LogicalOperator.AND);
    when(modelForUpdate.getRecipients()).thenReturn(Collections.singleton("OWNER"));
    when(modelForUpdate.getLaunchNames()).thenReturn(Collections.singleton("test launch1"));
    when(modelForUpdate.isEnabled()).thenReturn(true);
    when(modelForUpdate.getProject()).thenReturn(project);

    SenderCase dupeUpdateNotification = mock(SenderCase.class);
    when(dupeUpdateNotification.getId()).thenReturn(2L);
    when(dupeUpdateNotification.getSendCase()).thenReturn(SendCase.ALWAYS);
    when(dupeUpdateNotification.getRuleName()).thenReturn("Rule3");
    when(dupeUpdateNotification.getAttributesOperator()).thenReturn(LogicalOperator.AND);
    when(dupeUpdateNotification.getRecipients()).thenReturn(Collections.singleton("OWNER"));
    when(dupeUpdateNotification.getLaunchNames()).thenReturn(Collections.singleton("test launch"));
    when(dupeUpdateNotification.isEnabled()).thenReturn(true);
    when(dupeUpdateNotification.getProject()).thenReturn(project);

    LaunchAttributeRule launchAttribute = mock(LaunchAttributeRule.class);
    when(launchAttribute.getKey()).thenReturn("key");
    when(launchAttribute.getValue()).thenReturn("val");
    when(dupeUpdateNotification.getLaunchAttributeRules()).thenReturn(
        Collections.singleton(launchAttribute));

    when(senderCaseRepository.findAllByProjectId(DEFAULT_PROJECT_ID)).thenReturn(
        List.of(modelForUpdate, dupeUpdateNotification));

    assertTrue(assertThrows(ReportPortalException.class,
        () -> service.updateNotification(project, updateNotificationRQ, rpUser)
    ).getMessage().contains("Project notification settings contain duplicate cases"));
  }

}
