/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.logtype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.ReportPortalUserUtil;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.core.events.activity.LogTypeDeletedEvent;
import com.epam.reportportal.core.logtype.validator.LogTypeValidator;
import com.epam.reportportal.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class DeleteLogTypeHandlerImplTest {

  private static final String PROJECT_NAME = "default_personal";
  private static final Long PROJECT_ID = 1L;
  private static final Long LOG_TYPE_ID = 10L;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private LogTypeRepository logTypeRepository;

  @Mock
  private LogTypeValidator logTypeValidator;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private DeleteLogTypeHandlerImpl handler;

  @Test
  void deleteLogTypeWhenValidShouldDeleteSuccessfully() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType logType = createLogType(LOG_TYPE_ID, PROJECT_ID, "custom", 9000, false);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(logType));
    doNothing().when(logTypeValidator).validateLogTypeBelongsToProject(logType, PROJECT_ID);

    // When
    handler.deleteLogType(PROJECT_NAME, LOG_TYPE_ID, user);

    // Then
    verify(logTypeRepository).delete(logType);
    verify(eventPublisher).publishEvent(any(LogTypeDeletedEvent.class));
  }

  @Test
  void deleteLogTypeWhenProjectNotFoundShouldThrowProjectNotFound() {
    // Given
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        OrganizationRole.MEMBER, ProjectRole.EDITOR, PROJECT_ID);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.empty());

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.deleteLogType(PROJECT_NAME, LOG_TYPE_ID, user));

    // Then
    assertEquals(ErrorType.PROJECT_NOT_FOUND, ex.getErrorType());
  }

  @Test
  void deleteLogTypeWhenLogTypeNotFoundShouldThrowNotFound() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, PROJECT_ID);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.empty());

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.deleteLogType(PROJECT_NAME, LOG_TYPE_ID, user));

    // Then
    assertEquals(ErrorType.NOT_FOUND, ex.getErrorType());
  }

  @Test
  void deleteLogTypeWhenLogTypeBelongsToDifferentProjectShouldThrowForbiddenOperation() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType logType = createLogType(LOG_TYPE_ID, 2L, "custom", 9000, false);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(logType));
    doThrow(new ReportPortalException(ErrorType.ACCESS_DENIED, LOG_TYPE_ID,
        "Log type '10' does not belong to the specified project"))
        .when(logTypeValidator).validateLogTypeBelongsToProject(logType, PROJECT_ID);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.deleteLogType(PROJECT_NAME, LOG_TYPE_ID, user));

    // Then
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
  }

  @Test
  void deleteLogTypeWhenLogTypeIsSystemShouldThrowAccessDenied() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType logType = createLogType(LOG_TYPE_ID, PROJECT_ID, "error", 40000, true);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(logType));
    doNothing().when(logTypeValidator).validateLogTypeBelongsToProject(logType, PROJECT_ID);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.deleteLogType(PROJECT_NAME, LOG_TYPE_ID, user));

    // Then
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
  }

  private ProjectLogType createLogType(Long id, Long projectId, String name, Integer level,
      boolean isSystem) {
    ProjectLogType logType = new ProjectLogType();
    logType.setId(id);
    logType.setProjectId(projectId);
    logType.setName(name);
    logType.setLevel(level);
    logType.setSystem(isSystem);
    return logType;
  }
}
