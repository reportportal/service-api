package com.epam.reportportal.base.infrastructure.persistence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogTypeResolverTest {

  @Mock
  private LogTypeRepository logTypeRepository;

  @InjectMocks
  private LogTypeResolver logTypeResolver;

  @Test
  void resolveLevelWhenLevelNameIsNullShouldReturnUnknownLevel() {
    // given
    Long projectId = 1L;
    String levelName = null;

    // when
    int result = logTypeResolver.resolveLogLevelFromName(projectId, levelName);

    // then
    assertEquals(LogLevel.UNKNOWN_INT, result);
    verifyNoInteractions(logTypeRepository);
  }

  @Test
  void resolveLevelWhenLevelNameIsEmptyShouldReturnUnknownLevel() {
    // given
    Long projectId = 1L;
    String levelName = "";

    // when
    int result = logTypeResolver.resolveLogLevelFromName(projectId, levelName);

    // then
    assertEquals(LogLevel.UNKNOWN_INT, result);
    verifyNoInteractions(logTypeRepository);
  }

  @Test
  void resolveLevelWhenLevelNameMatchesLogLevelEnumShouldReturnMatchingLevel() {
    // given
    Long projectId = 1L;
    String levelName = "INFO";

    // when
    int result = logTypeResolver.resolveLogLevelFromName(projectId, levelName);

    // then
    assertEquals(LogLevel.INFO_INT, result);
    verifyNoInteractions(logTypeRepository);
  }

  @Test
  void resolveLevelWhenNameDoesNotMatchEnumButExistsInDbShouldReturnDbValue() {
    // given
    Long projectId = 1L;
    String levelName = "CUSTOM_LEVEL";

    int expectedDbValue = 25000;
    when(logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(projectId, levelName))
        .thenReturn(Optional.of(expectedDbValue));

    // when
    int result = logTypeResolver.resolveLogLevelFromName(projectId, levelName);

    // then
    assertEquals(expectedDbValue, result);
    verify(logTypeRepository).findLevelByProjectIdAndNameIgnoreCase(projectId, levelName);
  }

  @Test
  void resolveLevelWhenNameDoesNotMatchEnumAndDbHasNoEntryShouldReturnUnknownLevel() {
    // given
    Long projectId = 1L;
    String levelName = "NON_EXISTENT_LEVEL";

    when(logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(projectId, levelName))
        .thenReturn(Optional.empty());

    // when
    int result = logTypeResolver.resolveLogLevelFromName(projectId, levelName);

    // then
    assertEquals(LogLevel.UNKNOWN_INT, result);
    verify(logTypeRepository).findLevelByProjectIdAndNameIgnoreCase(projectId, levelName);
  }

  @Test
  void resolveNameFromLogLevelWhenLogLevelMatchesEnumShouldReturnCorrectLevelName() {
    // given
    Long projectId = 1L;
    int logLevel = 40000;

    // when
    String result = logTypeResolver.resolveNameFromLogLevel(projectId, logLevel);

    // then
    assertEquals("ERROR", result);
    verifyNoInteractions(logTypeRepository);
  }

  @Test
  void resolveNameFromLogLevelWhenLogLevelMatchesCustomLevelShouldReturnName() {
    // given
    Long projectId = 1L;
    int logLevel = 35000;
    when(logTypeRepository.findNameByProjectIdAndLevel(projectId, logLevel))
        .thenReturn("CUSTOM_ERROR");

    // when
    String result = logTypeResolver.resolveNameFromLogLevel(projectId, logLevel);

    // then
    assertEquals("CUSTOM_ERROR", result);
    verify(logTypeRepository).findNameByProjectIdAndLevel(projectId, logLevel);
  }

  @Test
  void resolveNameFromLogLevelWhenLogLevelDoesNotMatchEnumAndDbHasNoEntryShouldReturnUnknown() {
    // given
    Long projectId = 1L;
    int logLevel = 99999;
    when(logTypeRepository.findNameByProjectIdAndLevel(projectId, logLevel))
        .thenReturn(null);

    // when
    String result = logTypeResolver.resolveNameFromLogLevel(projectId, logLevel);

    // then
    assertEquals("UNKNOWN", result);
    verify(logTypeRepository).findNameByProjectIdAndLevel(projectId, logLevel);
  }

  @Test
  void getLogLevelMapForProjectWhenDefaultLogTypesShouldReturnAllLevelsMap() {
    // given
    Long projectId = 1L;
    List<ProjectLogType> projectLogTypes = List.of(
        createProjectLogType(projectId, "trace", 5000),
        createProjectLogType(projectId, "debug", 10000),
        createProjectLogType(projectId, "info", 20000),
        createProjectLogType(projectId, "warn", 30000),
        createProjectLogType(projectId, "error", 40000),
        createProjectLogType(projectId, "fatal", 50000),
        createProjectLogType(projectId, "unknown", 60000)
    );
    when(logTypeRepository.findByProjectId(projectId)).thenReturn(projectLogTypes);

    // when
    Map<Integer, String> result = logTypeResolver.getLogLevelMapForProject(projectId);

    // then
    assertNotNull(result);
    assertEquals(7, result.size());
    assertEquals("trace", result.get(5000));
    assertEquals("debug", result.get(10000));
    assertEquals("info", result.get(20000));
    assertEquals("warn", result.get(30000));
    assertEquals("error", result.get(40000));
    assertEquals("fatal", result.get(50000));
    assertEquals("unknown", result.get(60000));
    verify(logTypeRepository).findByProjectId(projectId);
  }

  @Test
  void getLogLevelMapForProjectWheCustomLogTypesShouldReturnAllLevelsMap() {
    // given
    Long projectId = 2L;
    List<ProjectLogType> projectLogTypes = List.of(
        createProjectLogType(projectId, "trace", 5000),
        createProjectLogType(projectId, "debug", 10000),
        createProjectLogType(projectId, "info", 20000),
        createProjectLogType(projectId, "custom_warning", 25000),
        createProjectLogType(projectId, "warn", 30000),
        createProjectLogType(projectId, "custom_critical", 35000),
        createProjectLogType(projectId, "error", 40000),
        createProjectLogType(projectId, "fatal", 50000),
        createProjectLogType(projectId, "unknown", 60000)
    );
    when(logTypeRepository.findByProjectId(projectId)).thenReturn(projectLogTypes);

    // when
    Map<Integer, String> result = logTypeResolver.getLogLevelMapForProject(projectId);

    // then
    assertNotNull(result);
    assertEquals(9, result.size());
    assertEquals("custom_warning", result.get(25000));
    assertEquals("custom_critical", result.get(35000));
    assertTrue(result.containsKey(5000));
    assertTrue(result.containsKey(10000));
    assertTrue(result.containsKey(20000));
    assertTrue(result.containsKey(30000));
    assertTrue(result.containsKey(40000));
    assertTrue(result.containsKey(50000));
    assertTrue(result.containsKey(60000));
    verify(logTypeRepository).findByProjectId(projectId);
  }

  private ProjectLogType createProjectLogType(Long projectId, String name, int level) {
    ProjectLogType logType = new ProjectLogType();
    logType.setProjectId(projectId);
    logType.setName(name);
    logType.setLevel(level);
    return logType;
  }

}
