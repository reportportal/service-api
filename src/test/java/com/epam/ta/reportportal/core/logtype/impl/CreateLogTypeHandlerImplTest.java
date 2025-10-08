package com.epam.ta.reportportal.core.logtype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.LogType;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.reportportal.api.model.LogTypeStyle.TextStyleEnum;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import com.epam.ta.reportportal.entity.project.Project;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @InjectMocks
  private CreateLogTypeHandlerImpl handler;


  @Test
  void createLogTypeWhenProjectExistsShouldValidateAndSaveLogType() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    LogType style = createDefaultLogTypeStyle(LOG_TYPE_NAME, LOG_TYPE_LEVEL, true);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(PROJECT_ID, LOG_TYPE_NAME,
        LOG_TYPE_LEVEL)).thenReturn(false);
    when(logTypeRepository.countFilterableLogTypes(PROJECT_ID)).thenReturn(5L);
    when(logTypeRepository.save(any(ProjectLogType.class))).thenAnswer(invocation -> {
      ProjectLogType saved = invocation.getArgument(0);
      saved.setId(10L);
      return saved;
    });

    // When
    LogType created = handler.createLogType(PROJECT_NAME, style);

    // Then
    assertEquals(10L, created.getId());
    assertEquals(LOG_TYPE_NAME, created.getName());
    assertEquals(LOG_TYPE_LEVEL, created.getLevel());
    assertNotNull(created.getStyle());
    assertEquals("#4DB6AC", created.getStyle().getLabelColor());
    assertEquals("#FFFFFF", created.getStyle().getBackgroundColor());
    assertEquals("#445A47", created.getStyle().getTextColor());
    assertEquals("normal", created.getStyle().getTextStyle().getValue());
  }

  @Test
  void createLogTypeWhenProjectMissingShouldThrowProjectNotFound() {
    // Given
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.empty());

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.createLogType(PROJECT_NAME,
            createDefaultLogTypeStyle(LOG_TYPE_NAME, LOG_TYPE_LEVEL, true)));

    // Then
    assertEquals(ErrorType.PROJECT_NOT_FOUND, ex.getErrorType());
  }

  @Test
  void createLogTypeWhenDuplicateExistsShouldThrowResourceAlreadyExists() {
    // Given
    Project project = new Project(PROJECT_ID, PROJECT_NAME);
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(PROJECT_ID, LOG_TYPE_NAME,
        LOG_TYPE_LEVEL)).thenReturn(true);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.createLogType(PROJECT_NAME,
            createDefaultLogTypeStyle(LOG_TYPE_NAME, LOG_TYPE_LEVEL, true)));

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
    when(projectRepository.findByName(PROJECT_NAME)).thenReturn(Optional.of(project));
    when(logTypeRepository.existsByProjectIdAndNameOrLevelIgnoreCase(PROJECT_ID, LOG_TYPE_NAME,
        LOG_TYPE_LEVEL)).thenReturn(false);
    when(logTypeRepository.countFilterableLogTypes(PROJECT_ID)).thenReturn(6L);

    // When
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> handler.createLogType(PROJECT_NAME,
            createDefaultLogTypeStyle(LOG_TYPE_NAME, LOG_TYPE_LEVEL, true)));

    // Then
    assertEquals(ErrorType.BAD_REQUEST_ERROR, ex.getErrorType());
    assertEquals(
        "Error in handled Request. Please, check specified parameters: 'Cannot create more than 6 filterable log types per project.'",
        ex.getMessage());
  }

  private LogType createDefaultLogTypeStyle(String name, Integer level, Boolean isFilterable) {
    LogType logType = new LogType();
    logType.setName(name);
    logType.setLevel(level);
    logType.setStyle(getDefaultLogTypeStyle());
    logType.setIsFilterable(isFilterable);
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
