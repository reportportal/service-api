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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.dao.SenderCaseRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.SenderCase;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:chingiskhan_kalanov@epam.com">Chingiskhan Kalanov</a>
 */
class DeleteProjectNotificationHandlerImplTest {

  private final SenderCaseRepository senderCaseRepository = mock(SenderCaseRepository.class);
  private final MessageBus messageBus = mock(MessageBus.class);
  private final ProjectConverter projectConverter = mock(ProjectConverter.class);

  private final DeleteProjectNotificationHandlerImpl service = new DeleteProjectNotificationHandlerImpl(
      senderCaseRepository, messageBus,
      projectConverter);

  private Project project;
  private ReportPortalUser rpUser;

  @BeforeEach
  public void beforeEach() {
    project = mock(Project.class);
    when(project.getId()).thenReturn(1L);

    rpUser = mock(ReportPortalUser.class);
  }

  @Test
  public void deleteNonExistingNotificationTest() {
    Assertions.assertTrue(
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

    Assertions.assertTrue(
        assertThrows(ReportPortalException.class,
            () -> service.deleteNotification(project, 1L, rpUser))
            .getMessage().contains("Did you use correct Notification ID?")
    );
  }

}
