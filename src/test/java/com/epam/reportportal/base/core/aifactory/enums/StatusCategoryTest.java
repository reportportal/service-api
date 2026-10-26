package com.epam.reportportal.base.core.aifactory.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatusCategoryTest {

  @ParameterizedTest
  @CsvSource({
      "Pending,PENDING", "running,RUNNING", "RUNNING,RUNNING", "Passed,PASSED", "Done,PASSED", "Completed,PASSED",
      "Failed,FAILED", "error,FAILED", "Skipped,SKIPPED", "In progress,WAITING", "In review,WAITING",
      "NEEDS_HUMAN,WAITING", "needs-human,WAITING", "Queued · waiting for runner,UNKNOWN"
  })
  void fromReported_ShouldMapWellKnownTextsAndLeaveTheRestUnknown(String text, StatusCategory expected) {
    assertEquals(expected, StatusCategory.fromReported(text));
  }

  @Test
  void fromReported_WithNull_ShouldBeUnknown() {
    assertEquals(StatusCategory.UNKNOWN, StatusCategory.fromReported(null));
  }

  @Test
  void aggregate_ShouldTakeTheWorstOfFailedRunningWaitingPendingPassed() {
    assertEquals(StatusCategory.FAILED, StatusCategory.aggregate(Set.of(StatusCategory.PASSED, StatusCategory.FAILED,
        StatusCategory.RUNNING)));
    assertEquals(StatusCategory.RUNNING, StatusCategory.aggregate(Set.of(StatusCategory.WAITING,
        StatusCategory.RUNNING, StatusCategory.PENDING)));
    assertEquals(StatusCategory.WAITING, StatusCategory.aggregate(Set.of(StatusCategory.WAITING,
        StatusCategory.PENDING, StatusCategory.PASSED)));
    assertEquals(StatusCategory.PENDING, StatusCategory.aggregate(Set.of(StatusCategory.PENDING,
        StatusCategory.PASSED)));
    assertEquals(StatusCategory.PASSED, StatusCategory.aggregate(List.of(StatusCategory.PASSED)));
  }

  @Test
  void aggregate_WithNothingToJudgeBy_ShouldBePendingPassedOrUnknown() {
    assertEquals(StatusCategory.PENDING, StatusCategory.aggregate(List.of()));
    assertEquals(StatusCategory.PASSED, StatusCategory.aggregate(List.of(StatusCategory.SKIPPED)));
    assertEquals(StatusCategory.UNKNOWN, StatusCategory.aggregate(List.of(StatusCategory.UNKNOWN)));
  }

  @Test
  void defaultLabel_ShouldBeReadable() {
    assertEquals("Running", StatusCategory.RUNNING.defaultLabel());
    assertEquals("Failed", StatusCategory.FAILED.defaultLabel());
  }
}
