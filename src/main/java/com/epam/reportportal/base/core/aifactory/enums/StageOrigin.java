package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Where a stage comes from, which also tells who keeps it up to date.
 * <p>
 * Example: in a test case generation iteration the Create, Grade and Upload stages are {@link #REPORTED} by
 * the agent's CI job, while the Review stage is {@link #DERIVED}: ReportPortal creates it and recounts "2 / 3
 * Ready" whenever a reviewer changes a test case, so the CI job never reports it.
 */
@Schema(description = "REPORTED - sent by the pipeline (e.g. Create, Grade, Upload); DERIVED - created and kept "
    + "up to date by ReportPortal from other data (e.g. a Review stage counting how many test cases are Ready)")
public enum StageOrigin {
  /** Sent by the pipeline itself. */
  REPORTED,
  /** Calculated and maintained by ReportPortal; the pipeline cannot report it. */
  DERIVED
}
