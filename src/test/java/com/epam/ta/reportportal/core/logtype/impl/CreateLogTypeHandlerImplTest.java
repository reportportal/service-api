package com.epam.ta.reportportal.core.logtype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.reportportal.api.model.LogTypeStyle.TextStyleEnum;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ReportPortalUserUtil;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LogTypeCreatedEvent;
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
class CreateLogTypeHandlerImplTest {

  private static final String PROJECT_NAME = "default_personal";
  private static final Long PROJECT_ID = 1L;
  private static final String LOG_TYPE_NAME = "INFO";
  private static final Integer LOG_TYPE_LEVEL = 20000;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private LogTypeRepository logTypeRepository;

  @Mock
  private LogTypeValidator logTypeValidator;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private CreateLogTypeHandlerImpl handler;


  @Test
  void createLogTypeWhenProjectExistsShouldValidateAndSaveLogType() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    LogTypeRequest style = createDefaultLogTypeStyle();
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);

    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    doNothing().when(logTypeValidator).validateUniqueness(anyLong(), anyString(), anyInt());
    doNothing().when(logTypeValidator).validateFilterableLimit(anyLong(), anyBoolean());
    when(logTypeRepository.save(any(ProjectLogType.class))).thenAnswer(invocation -> {
      ProjectLogType saved = invocation.getArgument(0);
      saved.setId(10L);
      return saved;
    });

    // When
    LogTypeResponse created = handler.createLogType(PROJECT_NAME, style, user);

    // Then
    assertEquals(10L, created.getId());
    assertEquals(LOG_TYPE_NAME, created.getName());
    assertEquals(LOG_TYPE_LEVEL, created.getLevel());
    assertNotNull(created.getStyle());
    assertEquals("#4DB6AC", created.getStyle().getLabelColor());
    assertEquals("#FFFFFF", created.getStyle().getBackgroundColor());
    assertEquals("#445A47", created.getStyle().getTextColor());
    assertEquals("normal", created.getStyle().getTextStyle().getValue());
    verify(eventPublisher).publishEvent(any(LogTypeCreatedEvent.class));
  }

  @Test
  void createLogTypeWhenProjectMissingShouldThrowProjectNotFound() {
    // Given
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.empty());

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.createLogType(PROJECT_NAME,
            createDefaultLogTypeStyle(), user));

    // Then
    assertEquals(ErrorType.PROJECT_NOT_FOUND, ex.getErrorType());
  }

  @Test
  void createLogTypeWhenDuplicateExistsShouldThrowResourceAlreadyExists() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    doThrow(new ReportPortalException(ErrorType.RESOURCE_ALREADY_EXISTS,
        "Log type: INFO - 20000")).when(logTypeValidator)
        .validateUniqueness(PROJECT_ID, LOG_TYPE_NAME, LOG_TYPE_LEVEL);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.createLogType(PROJECT_NAME,
            createDefaultLogTypeStyle(), user));

    // Then
    assertEquals(ErrorType.RESOURCE_ALREADY_EXISTS, ex.getErrorType());
    assertEquals(
        "Resource 'Log type: INFO - 20000' already exists. You couldn't create the duplicate.",
        ex.getMessage());
  }

  @Test
  void createLogTypeWhenFilterableLimitExceededShouldThrowBadRequestError() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    ReportPortalUser user = ReportPortalUserUtil.getRpUser("user", UserRole.USER,
        ProjectRole.PROJECT_MANAGER, PROJECT_ID);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    doNothing().when(logTypeValidator)
        .validateUniqueness(PROJECT_ID, LOG_TYPE_NAME, LOG_TYPE_LEVEL);
    doThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
        "Cannot set more than 6 filterable log types per project. Current count: 6")).when(
        logTypeValidator).validateFilterableLimit(PROJECT_ID, true);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.createLogType(PROJECT_NAME,
            createDefaultLogTypeStyle(), user));

    // Then
    assertEquals(ErrorType.BAD_REQUEST_ERROR, ex.getErrorType());
  }

  private LogTypeRequest createDefaultLogTypeStyle() {
    LogTypeRequest logType = new LogTypeRequest();
    logType.setName(CreateLogTypeHandlerImplTest.LOG_TYPE_NAME);
    logType.setLevel(CreateLogTypeHandlerImplTest.LOG_TYPE_LEVEL);
    logType.setStyle(getDefaultLogTypeStyle());
    logType.setIsFilterable(true);
    return logType;
  }

  private LogTypeStyle getDefaultLogTypeStyle() {
    LogTypeStyle style = new LogTypeStyle();
    style.setLabelColor("#4DB6AC");
    style.setBackgroundColor("#FFFFFF");
    style.setTextColor("#445A47");
    style.setTextStyle(TextStyleEnum.NORMAL);
    return style;
  }
}
