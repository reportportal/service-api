package com.epam.reportportal.core.logtype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultLogTypeProviderTest {

  private final DefaultLogTypeProvider defaultLogTypeProvider = new DefaultLogTypeProvider();

  @Test
  void provideDefaultLogTypesShouldReturnCorrectNumberOfLogTypes() {
    // given
    Long projectId = 1L;

    // when
    List<ProjectLogType> logTypes = defaultLogTypeProvider.provideDefaultLogTypes(projectId);

    // then
    assertThat(logTypes).hasSize(7);
    assertLogType(logTypes.get(0), projectId, "unknown", 60000, "#E3E7EC", false);
    assertLogType(logTypes.get(1), projectId, "fatal", 50000, "#8B0000", true);
    assertLogType(logTypes.get(2), projectId, "error", 40000, "#DC5959", true);
    assertLogType(logTypes.get(3), projectId, "warn", 30000, "#FFBC6C", true);
    assertLogType(logTypes.get(4), projectId, "info", 20000, "#23A6DE", true);
    assertLogType(logTypes.get(5), projectId, "debug", 10000, "#C1C7D0", true);
    assertLogType(logTypes.get(6), projectId, "trace", 5000, "#E3E7EC", true);
  }

  private void assertLogType(ProjectLogType logType, Long expectedProjectId, String expectedName,
      int expectedLevel, String expectedLabelColor, boolean expectedFilterable) {
    assertAll(
        () -> assertEquals(expectedProjectId, logType.getProjectId()),
        () -> assertEquals(expectedName, logType.getName()),
        () -> assertEquals(expectedLevel, logType.getLevel()),
        () -> assertEquals(expectedLabelColor, logType.getLabelColor()),
        () -> assertEquals(expectedFilterable, logType.isFilterable()),
        () -> assertTrue(logType.isSystem()),
        () -> assertEquals("#FFFFFF", logType.getBackgroundColor()),
        () -> assertEquals("#464547", logType.getTextColor()),
        () -> assertEquals("normal", logType.getTextStyle())
    );
  }
}
