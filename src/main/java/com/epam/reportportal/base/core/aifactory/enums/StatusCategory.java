package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

/**
 * Normalized meaning of a reported status. The status text itself is stored exactly as the pipeline
 * reported it; the category is what aggregation and colouring rely on, so an unknown text such as
 * "Queued · waiting for runner" stays displayable while the pipeline (or {@link #fromReported}) decides
 * how it counts.
 */
@Schema(description = "Normalized category of a reported status. An iteration takes the worst category of the stages "
    + "that affect its status: FAILED > TIMED_OUT > INTERRUPTED > RUNNING > WAITING > PENDING > PASSED. SKIPPED and "
    + "UNKNOWN stages are ignored. TIMED_OUT is a stage or run that was stopped because it took too long, "
    + "INTERRUPTED one that was stopped from outside (cancelled, aborted). Clients should render UNKNOWN neutrally "
    + "(gray) and show the reported status text.")
public enum StatusCategory {
  PENDING,
  RUNNING,
  WAITING,
  PASSED,
  FAILED,
  TIMED_OUT,
  INTERRUPTED,
  SKIPPED,
  UNKNOWN;

  private static final Set<String> PENDING_TEXTS = Set.of("pending");
  private static final Set<String> RUNNING_TEXTS = Set.of("running");
  private static final Set<String> PASSED_TEXTS = Set.of("passed", "done", "completed", "success", "succeeded");
  private static final Set<String> FAILED_TEXTS = Set.of("failed", "failure", "error");
  private static final Set<String> TIMED_OUT_TEXTS = Set.of("timed out", "timeout");
  private static final Set<String> INTERRUPTED_TEXTS = Set.of("interrupted", "cancelled", "canceled", "aborted");
  private static final Set<String> SKIPPED_TEXTS = Set.of("skipped");
  private static final Set<String> WAITING_TEXTS = Set.of("in progress", "in review", "needs human", "waiting");

  /**
   * Category of a status text the pipeline reported without an explicit category; anything not
   * recognized is {@link #UNKNOWN}.
   */
  public static StatusCategory fromReported(String status) {
    if (status == null) {
      return UNKNOWN;
    }
    var normalized = status.trim().toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ');
    if (PENDING_TEXTS.contains(normalized)) {
      return PENDING;
    }
    if (RUNNING_TEXTS.contains(normalized)) {
      return RUNNING;
    }
    if (PASSED_TEXTS.contains(normalized)) {
      return PASSED;
    }
    if (FAILED_TEXTS.contains(normalized)) {
      return FAILED;
    }
    if (TIMED_OUT_TEXTS.contains(normalized)) {
      return TIMED_OUT;
    }
    if (INTERRUPTED_TEXTS.contains(normalized)) {
      return INTERRUPTED;
    }
    if (SKIPPED_TEXTS.contains(normalized)) {
      return SKIPPED;
    }
    if (WAITING_TEXTS.contains(normalized)) {
      return WAITING;
    }
    return UNKNOWN;
  }

  /**
   * Iteration category from the categories of the stages that affect it:
   * FAILED &gt; TIMED_OUT &gt; INTERRUPTED &gt; RUNNING &gt; WAITING &gt; PENDING &gt; PASSED; SKIPPED and UNKNOWN
   * are ignored.
   * Nothing left to judge by is PENDING when there are no stages, UNKNOWN when only unknown ones
   * were reported and PASSED when everything was skipped.
   */
  public static StatusCategory aggregate(Collection<StatusCategory> categories) {
    if (categories.isEmpty()) {
      return PENDING;
    }
    if (categories.contains(FAILED)) {
      return FAILED;
    }
    if (categories.contains(TIMED_OUT)) {
      return TIMED_OUT;
    }
    if (categories.contains(INTERRUPTED)) {
      return INTERRUPTED;
    }
    if (categories.contains(RUNNING)) {
      return RUNNING;
    }
    if (categories.contains(WAITING)) {
      return WAITING;
    }
    if (categories.contains(PENDING)) {
      return PENDING;
    }
    if (categories.contains(PASSED)) {
      return PASSED;
    }
    return categories.contains(SKIPPED) ? PASSED : UNKNOWN;
  }

  /**
   * Whether work in this category ended without succeeding: it failed, ran out of time or was stopped. A run
   * that ended this way will not report anything more, so what waits for its result has to be released.
   */
  public boolean endedUnsuccessfully() {
    return this == FAILED || this == TIMED_OUT || this == INTERRUPTED;
  }

  /**
   * Default text for a status derived (not reported), e.g. an iteration's.
   */
  public String defaultLabel() {
    return switch (this) {
      case PENDING -> "Pending";
      case RUNNING -> "Running";
      case WAITING -> "Waiting";
      case PASSED -> "Passed";
      case FAILED -> "Failed";
      case TIMED_OUT -> "Timed out";
      case INTERRUPTED -> "Interrupted";
      case SKIPPED -> "Skipped";
      case UNKNOWN -> "Unknown";
    };
  }
}
