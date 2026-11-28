package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import java.time.Instant;

/**
 * Automatic milestone status (Scheduled, Testing, Completed) driven by the three options stored on
 * the milestone: start on the start date, start with the first execution, complete after the
 * deadline. The rules only move a milestone forward; a milestone without options is never changed.
 */
public interface TmsMilestoneAutoStatusService {

  /**
   * Applies the automatic rules to a milestone that is about to be saved (create, edit,
   * duplicate). Changes only the status of the given entity.
   *
   * @param milestone the milestone entity
   * @param now       the current time
   */
  void applyTo(TmsMilestone milestone, Instant now);

  /**
   * Applies the automatic rules to all milestones (scheduled job).
   *
   * @param now the current time
   * @return number of status changes
   */
  int applyToAll(Instant now);

  /**
   * Moves the milestone of the launch's test plan from Scheduled to Testing when it has the "start
   * with the first execution" option. Called when a test execution of the launch gets a status.
   *
   * @param launchId the launch ID
   */
  void onTestExecuted(Long launchId);
}
