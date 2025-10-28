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

package com.epam.ta.reportportal.core.logtype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.reportportal.api.model.LogTypeStyle.TextStyleEnum;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ReportPortalUserUtil;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LogTypeUpdatedEvent;
import com.epam.ta.reportportal.core.logtype.validator.LogTypeValidator;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class UpdateLogTypeHandlerImplTest {

  private static final String PROJECT_NAME = "default_personal";
  private static final Long PROJECT_ID = 1L;
  private static final Long LOG_TYPE_ID = 10L;
  private static final String LOG_TYPE_NAME = "custom";
  private static final Integer LOG_TYPE_LEVEL = 9000;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private LogTypeRepository logTypeRepository;

  @Mock
  private LogTypeValidator logTypeValidator;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private UpdateLogTypeHandlerImpl handler;

  @Test
  void updateLogTypeWhenValidShouldValidateAndUpdateSuccessfully() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType existingLogType = createLogType(LOG_TYPE_NAME, LOG_TYPE_LEVEL, false);
    LogTypeRequest updateRequest = createUpdateRequest("updated-name", 9500);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(existingLogType));
    when(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(PROJECT_ID,
        "updated-name", 9500, LOG_TYPE_ID)).thenReturn(false);
    doNothing().when(logTypeValidator).validateFilterableLimit(PROJECT_ID, true);
    when(logTypeRepository.save(any(ProjectLogType.class))).thenAnswer(invocation -> {
      ProjectLogType saved = invocation.getArgument(0);
      saved.setId(LOG_TYPE_ID);
      return saved;
    });

    // When
    SuccessfulUpdate successfulUpdate = handler.updateLogType(PROJECT_NAME, LOG_TYPE_ID,
        updateRequest, user);

    // Then
    assertEquals("The update was completed successfully.", successfulUpdate.getMessage());
    verify(eventPublisher).publishEvent(any(LogTypeUpdatedEvent.class));
  }

  @Test
  void updateLogTypeWhenSystemLogTypeNameChangedShouldThrowAccessDenied() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType existingLogType = createLogType("error", 40000, true);
    LogTypeRequest updateRequest = createUpdateRequest("error upd", 40000);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(existingLogType));

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.updateLogType(PROJECT_NAME, LOG_TYPE_ID, updateRequest, user));

    // Then
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    assertEquals(
        "You do not have enough permissions. Cannot modify name of system log type 'error'",
        ex.getMessage());

  }

  @Test
  void updateLogTypeWhenSystemLogTypeLevelChangedShouldThrowAccessDenied() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType existingLogType = createLogType("error", 40000, true);
    LogTypeRequest updateRequest = createUpdateRequest("error", 50000);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(existingLogType));

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.updateLogType(PROJECT_NAME, LOG_TYPE_ID, updateRequest, user));

    // Then
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    assertEquals(
        "You do not have enough permissions. Cannot modify level of system log type 'error'",
        ex.getMessage());

  }

  @Test
  void updateLogTypeWhenLogTypeLevelChangedShouldValidateUniqueness() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType existingLogType = createLogType("custom", 42000, false);
    LogTypeRequest updateRequest = createUpdateRequest("custom new", 50000);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(existingLogType));
    when(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(PROJECT_ID,
        "custom new", 50000, LOG_TYPE_ID)).thenReturn(true);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.updateLogType(PROJECT_NAME, LOG_TYPE_ID, updateRequest, user));

    // Then
    assertEquals(ErrorType.RESOURCE_ALREADY_EXISTS, ex.getErrorType());
  }

  @Test
  void updateLogTypeWhenUnknownLogTypeSetAsFilterableShouldThrowAccessDenied() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType existingLogType = createLogType("unknown", 10000, false);
    LogTypeRequest updateRequest = createUpdateRequest("unknown", 10000);
    updateRequest.setIsFilterable(true);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(existingLogType));

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.updateLogType(PROJECT_NAME, LOG_TYPE_ID, updateRequest, user));

    // Then
    assertEquals(ErrorType.ACCESS_DENIED, ex.getErrorType());
    assertEquals(
        "You do not have enough permissions. The 'unknown' log type cannot be set as filterable",
        ex.getMessage());
  }

  @Test
  void updateLogTypeWhenOnlyNameChangedShouldNotThrowDuplicateError() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ProjectLogType existingLogType = createLogType("custom error", 50002, false);
    LogTypeRequest updateRequest = createUpdateRequest("custom error updated", 50002);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.findById(LOG_TYPE_ID)).thenReturn(Optional.of(existingLogType));
    when(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCaseExcludingId(PROJECT_ID,
        "custom error updated", 50002, LOG_TYPE_ID)).thenReturn(false);
    doNothing().when(logTypeValidator).validateFilterableLimit(PROJECT_ID, true);
    when(logTypeRepository.save(any(ProjectLogType.class))).thenAnswer(invocation -> {
      ProjectLogType saved = invocation.getArgument(0);
      saved.setId(LOG_TYPE_ID);
      return saved;
    });

    // When
    SuccessfulUpdate successfulUpdate = handler.updateLogType(PROJECT_NAME, LOG_TYPE_ID,
        updateRequest, user);

    // Then
    assertEquals("The update was completed successfully.", successfulUpdate.getMessage());
    verify(eventPublisher).publishEvent(any(LogTypeUpdatedEvent.class));
  }

  private ProjectLogType createLogType(String name, Integer level, boolean isSystem) {
    var logType = new ProjectLogType();
    logType.setId(UpdateLogTypeHandlerImplTest.LOG_TYPE_ID);
    logType.setProjectId(UpdateLogTypeHandlerImplTest.PROJECT_ID);
    logType.setName(name);
    logType.setLevel(level);
    logType.setSystem(isSystem);
    logType.setFilterable(false);
    logType.setLabelColor("#4DB6AC");
    logType.setBackgroundColor("#FFFFFF");
    logType.setTextColor("#445A47");
    logType.setTextStyle("normal");
    return logType;
  }

  private LogTypeRequest createUpdateRequest(String name, Integer level) {
    var logType = new LogTypeRequest();
    logType.setName(name);
    logType.setLevel(level);
    logType.setIsFilterable(true);
    logType.setStyle(getDefaultLogTypeStyle());
    return logType;
  }

  private LogTypeStyle getDefaultLogTypeStyle() {
    var style = new LogTypeStyle();
    style.setLabelColor("#4DB6AC");
    style.setBackgroundColor("#FFFFFF");
    style.setTextColor("#445A47");
    style.setTextStyle(TextStyleEnum.NORMAL);
    return style;
  }
}
