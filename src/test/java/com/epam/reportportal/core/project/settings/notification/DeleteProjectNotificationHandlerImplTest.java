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

import com.epam.reportportal.core.events.activity.converter.NotificationRuleDeletedEventConverter;
import com.epam.reportportal.core.events.domain.NotificationRuleDeletedEvent;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.SenderCaseRepository;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.email.SenderCase;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.project.ProjectConfiguration;
import com.epam.reportportal.model.project.ProjectResource;
import com.epam.reportportal.model.project.email.ProjectNotificationConfigDTO;
import com.epam.reportportal.ws.converter.converters.ProjectConverter;
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
class DeleteProjectNotificationHandlerImplTest {

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
  private final ProjectConverter projectConverter = new ProjectConverter();

  private final DeleteProjectNotificationHandlerImpl service = new DeleteProjectNotificationHandlerImpl(
      senderCaseRepository, eventPublisher,
      projectConverter);

  @Captor
  private ArgumentCaptor<NotificationRuleDeletedEvent> activityCaptor;

  private Project project;
  private ReportPortalUser rpUser;

  @BeforeEach
  public void beforeEach() {
    project = new Project();
    project.setId(1L);

    rpUser = mock(ReportPortalUser.class);
  }

  @Test
  public void deleteNonExistingNotificationTest() {
    assertTrue(
        assertThrows(ReportPortalException.class,
            () -> service.deleteNotification(project, 1L, rpUser))
            .getMessage().contains("Did you use correct Notification ID?")
    );
  }

  @Test
  public void deleteNotificationButWithDifferentProjectTest() {
    SenderCase sc = mock(SenderCase.class);
    Project scProject = mock(Project.class);

    when(scProject.getId()).thenReturn(2L);
    when(sc.getProject()).thenReturn(scProject);
    when(senderCaseRepository.findById(any())).thenReturn(Optional.of(sc));

    assertTrue(
        assertThrows(ReportPortalException.class,
            () -> service.deleteNotification(project, 1L, rpUser))
            .getMessage().contains("Did you use correct Notification ID?")
    );
  }

  @Test
  void deleteNotificationWhenRuleDeletedShouldPublishNotificationRuleDeletedEvent() {
    // given
    DeleteProjectNotificationHandlerImpl serviceReal = new DeleteProjectNotificationHandlerImpl(
        senderCaseRepository,
        eventPublisher, projectConverter);

    long ruleId = 55L;
    Project project = new Project();
    project.setId(7L);
    project.setOrganizationId(77L);

    projectConverter.TO_PROJECT_RESOURCE = p -> {
      ProjectResource pr = new ProjectResource();
      pr.setProjectId(p.getId());
      ProjectConfiguration cfg = new ProjectConfiguration();
      ProjectNotificationConfigDTO pcfg = new ProjectNotificationConfigDTO();
      cfg.setProjectConfig(pcfg);
      pr.setConfiguration(cfg);
      return pr;
    };

    SenderCase existing = new SenderCase();
    existing.setId(ruleId);
    existing.setProject(project);
    existing.setRuleName("rule-X");
    existing.setAttributesOperator(
        com.epam.reportportal.infrastructure.persistence.entity.enums.LogicalOperator.AND);
    existing.setSendCase(
        com.epam.reportportal.infrastructure.persistence.entity.enums.SendCase.ALWAYS);

    when(senderCaseRepository.findById(ruleId)).thenReturn(java.util.Optional.of(existing));
    ReportPortalUser user = mock(ReportPortalUser.class);
    when(user.getUserId()).thenReturn(5L);
    when(user.getUsername()).thenReturn("u1");

    // when
    serviceReal.deleteNotification(project, ruleId, user);

    // then
    verify(eventPublisher).publishEvent(activityCaptor.capture());
    var activityCaptorValue = activityCaptor.getValue();
    assertInstanceOf(NotificationRuleDeletedEvent.class, activityCaptorValue);
    NotificationRuleDeletedEventConverter converter = new NotificationRuleDeletedEventConverter();
    var activity = converter.convert((NotificationRuleDeletedEvent) activityCaptorValue);
    assertEquals("deleteNotificationRule", activity.getEventName());
    assertEquals(project.getId(), activity.getProjectId());
  }

}
