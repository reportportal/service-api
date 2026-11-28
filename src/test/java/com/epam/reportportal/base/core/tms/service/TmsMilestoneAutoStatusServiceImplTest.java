package com.epam.reportportal.base.core.tms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TmsMilestoneAutoStatusServiceImplTest {

  private static final Instant NOW = Instant.parse("2026-11-28T10:00:00Z");
  private static final LocalDateTime TODAY = LocalDateTime.of(2026, 11, 28, 0, 0);

  @Mock
  private TmsMilestoneRepository tmsMilestoneRepository;

  @InjectMocks
  private TmsMilestoneAutoStatusServiceImpl sut;

  private static TmsMilestone milestone(TmsMilestoneStatus status, LocalDateTime start,
      LocalDateTime end) {
    var milestone = new TmsMilestone();
    milestone.setId(1L);
    milestone.setStatus(status);
    milestone.setStartDate(start);
    milestone.setEndDate(end);
    return milestone;
  }

  @Test
  void applyTo_WithoutOptions_ShouldKeepStatus() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.minusDays(10),
        TODAY.minusDays(1));

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.SCHEDULED, milestone.getStatus());
    verify(tmsMilestoneRepository, never()).existsExecutedTestByMilestoneId(anyLong());
  }

  @Test
  void applyTo_StartOnStartDate_WhenStartDateIsToday_ShouldMoveToTesting() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY, TODAY.plusDays(7));
    milestone.setAutoStartOnStartDate(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.TESTING, milestone.getStatus());
  }

  @Test
  void applyTo_StartOnStartDate_WhenStartDateIsTomorrow_ShouldKeepScheduled() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.plusDays(1),
        TODAY.plusDays(7));
    milestone.setAutoStartOnStartDate(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.SCHEDULED, milestone.getStatus());
  }

  @Test
  void applyTo_StartOnStartDate_WithoutStartDate_ShouldKeepScheduled() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, null, null);
    milestone.setAutoStartOnStartDate(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.SCHEDULED, milestone.getStatus());
  }

  @Test
  void applyTo_StartWithFirstExecution_WhenTestExecuted_ShouldMoveToTesting() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.plusDays(3),
        TODAY.plusDays(7));
    milestone.setAutoStartOnFirstExecution(true);
    when(tmsMilestoneRepository.existsExecutedTestByMilestoneId(1L)).thenReturn(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.TESTING, milestone.getStatus());
  }

  @Test
  void applyTo_StartWithFirstExecution_WhenNothingExecuted_ShouldKeepScheduled() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.plusDays(3),
        TODAY.plusDays(7));
    milestone.setAutoStartOnFirstExecution(true);
    when(tmsMilestoneRepository.existsExecutedTestByMilestoneId(1L)).thenReturn(false);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.SCHEDULED, milestone.getStatus());
  }

  @Test
  void applyTo_StartWithFirstExecution_OnNewMilestone_ShouldNotQueryExecutions() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.plusDays(3),
        TODAY.plusDays(7));
    milestone.setId(null);
    milestone.setAutoStartOnFirstExecution(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.SCHEDULED, milestone.getStatus());
    verify(tmsMilestoneRepository, never()).existsExecutedTestByMilestoneId(anyLong());
  }

  @Test
  void applyTo_CompleteAfterDeadline_WhenDeadlineWasYesterday_ShouldMoveToCompleted() {
    var milestone = milestone(TmsMilestoneStatus.TESTING, TODAY.minusDays(7),
        TODAY.minusDays(1));
    milestone.setAutoCompleteAfterDeadline(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.COMPLETED, milestone.getStatus());
  }

  @Test
  void applyTo_CompleteAfterDeadline_WhenDeadlineIsToday_ShouldKeepTesting() {
    var milestone = milestone(TmsMilestoneStatus.TESTING, TODAY.minusDays(7), TODAY);
    milestone.setAutoCompleteAfterDeadline(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.TESTING, milestone.getStatus());
  }

  @Test
  void applyTo_CompleteAfterDeadline_WhenScheduled_ShouldKeepScheduled() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.minusDays(7),
        TODAY.minusDays(1));
    milestone.setAutoCompleteAfterDeadline(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.SCHEDULED, milestone.getStatus());
  }

  @Test
  void applyTo_StartAndComplete_WhenBothDatesPassed_ShouldMoveToCompleted() {
    var milestone = milestone(TmsMilestoneStatus.SCHEDULED, TODAY.minusDays(7),
        TODAY.minusDays(1));
    milestone.setAutoStartOnStartDate(true);
    milestone.setAutoCompleteAfterDeadline(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.COMPLETED, milestone.getStatus());
  }

  @Test
  void applyTo_Completed_ShouldNeverMoveBack() {
    var milestone = milestone(TmsMilestoneStatus.COMPLETED, TODAY.minusDays(7),
        TODAY.plusDays(1));
    milestone.setAutoStartOnStartDate(true);
    milestone.setAutoStartOnFirstExecution(true);
    milestone.setAutoCompleteAfterDeadline(true);

    sut.applyTo(milestone, NOW);

    assertEquals(TmsMilestoneStatus.COMPLETED, milestone.getStatus());
  }

  @Test
  void applyToAll_ShouldRunTheThreeRulesWithUtcBoundaries() {
    when(tmsMilestoneRepository.startScheduledOnStartDate(LocalDateTime.of(2026, 11, 28, 10, 0)))
        .thenReturn(2);
    when(tmsMilestoneRepository.startScheduledWithExecutedTests()).thenReturn(1);
    when(tmsMilestoneRepository.completeTestingAfterDeadline(TODAY)).thenReturn(3);

    assertEquals(6, sut.applyToAll(NOW));
  }

  @Test
  void onTestExecuted_ShouldStartMilestoneOfLaunch() {
    sut.onTestExecuted(5L);

    verify(tmsMilestoneRepository).startScheduledByLaunchId(5L);
  }

  @Test
  void onTestExecuted_WithoutLaunch_ShouldDoNothing() {
    sut.onTestExecuted(null);

    verify(tmsMilestoneRepository, never()).startScheduledByLaunchId(anyLong());
  }
}
