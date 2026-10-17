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
    + "that affect its status: FAILED > RUNNING > WAITING > PENDING > PASSED. SKIPPED and UNKNOWN stages are ignored. "
    + "Clients should render UNKNOWN neutrally (gray) and show the reported status text.")
public enum StatusCategory {
  PENDING,
  RUNNING,
  WAITING,
  PASSED,
  FAILED,
  SKIPPED,
  UNKNOWN;

  private static final Set<String> PENDING_TEXTS = Set.of("pending");
  private static final Set<String> RUNNING_TEXTS = Set.of("running");
  private static final Set<String> PASSED_TEXTS = Set.of("passed", "done", "completed", "success", "succeeded");
  private static final Set<String> FAILED_TEXTS = Set.of("failed", "failure", "error");
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
   * FAILED &gt; RUNNING &gt; WAITING &gt; PENDING &gt; PASSED; SKIPPED and UNKNOWN are ignored.
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
   * Default text for a status derived (not reported), e.g. an iteration's.
   */
  public String defaultLabel() {
    return switch (this) {
      case PENDING -> "Pending";
      case RUNNING -> "Running";
      case WAITING -> "Waiting";
      case PASSED -> "Passed";
      case FAILED -> "Failed";
      case SKIPPED -> "Skipped";
      case UNKNOWN -> "Unknown";
    };
  }
}
