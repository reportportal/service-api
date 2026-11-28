package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsMilestoneRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsMilestoneStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link TmsMilestoneAutoStatusService}. Milestone dates are stored as UTC
 * {@link LocalDateTime}; "the start date is reached" means {@code startDate <= now}, "the deadline
 * day is over" means {@code endDate} is before the start of the current UTC day.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsMilestoneAutoStatusServiceImpl implements TmsMilestoneAutoStatusService {

  private final TmsMilestoneRepository tmsMilestoneRepository;

  @Override
  public void applyTo(TmsMilestone milestone, Instant now) {
    var nowUtc = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

    if (milestone.getStatus() == TmsMilestoneStatus.SCHEDULED
        && (isStartDateReached(milestone, nowUtc) || hasExecutedTest(milestone))) {
      milestone.setStatus(TmsMilestoneStatus.TESTING);
    }

    if (milestone.getStatus() == TmsMilestoneStatus.TESTING
        && isDeadlineDayOver(milestone, startOfDay(nowUtc))) {
      milestone.setStatus(TmsMilestoneStatus.COMPLETED);
    }
  }

  @Override
  @Transactional
  public int applyToAll(Instant now) {
    var nowUtc = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

    var startedOnStartDate = tmsMilestoneRepository.startScheduledOnStartDate(nowUtc);
    var startedWithExecution = tmsMilestoneRepository.startScheduledWithExecutedTests();
    var completed = tmsMilestoneRepository.completeTestingAfterDeadline(startOfDay(nowUtc));

    var changed = startedOnStartDate + startedWithExecution + completed;
    if (changed > 0) {
      log.info("Automatic milestone status: {} started on start date, {} started with first "
          + "execution, {} completed after deadline", startedOnStartDate, startedWithExecution,
          completed);
    }
    return changed;
  }

  @Override
  @Transactional
  public void onTestExecuted(Long launchId) {
    if (launchId != null && tmsMilestoneRepository.startScheduledByLaunchId(launchId) > 0) {
      log.debug("Milestone of launch: {} moved to Testing by its first execution", launchId);
    }
  }

  private boolean isStartDateReached(TmsMilestone milestone, LocalDateTime nowUtc) {
    return milestone.isAutoStartOnStartDate()
        && milestone.getStartDate() != null
        && !milestone.getStartDate().isAfter(nowUtc);
  }

  private boolean hasExecutedTest(TmsMilestone milestone) {
    return milestone.isAutoStartOnFirstExecution()
        && milestone.getId() != null
        && tmsMilestoneRepository.existsExecutedTestByMilestoneId(milestone.getId());
  }

  private boolean isDeadlineDayOver(TmsMilestone milestone, LocalDateTime startOfTodayUtc) {
    return milestone.isAutoCompleteAfterDeadline()
        && milestone.getEndDate() != null
        && milestone.getEndDate().isBefore(startOfTodayUtc);
  }

  private static LocalDateTime startOfDay(LocalDateTime dateTime) {
    return dateTime.toLocalDate().atStartOfDay();
  }
}
