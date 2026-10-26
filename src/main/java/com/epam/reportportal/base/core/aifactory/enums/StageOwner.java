package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Who creates a stage and keeps it up to date.
 * <p>
 * Example: in a test case generation iteration the Create, Grade and Upload stages are owned by the
 * {@link #PIPELINE}: the agent's CI job reports them. The Review stage is owned by {@link #REPORTPORTAL}:
 * ReportPortal creates it and recounts "2 / 3 Ready" whenever a reviewer changes a test case, so the CI job
 * never reports it.
 */
@Schema(description = "PIPELINE - reported by the pipeline (e.g. Create, Grade, Upload); REPORTPORTAL - created and "
    + "kept up to date by ReportPortal from other data (e.g. a Review stage counting how many test cases are Ready)")
public enum StageOwner {
  /** Reported by the pipeline itself. */
  PIPELINE,
  /** Calculated and maintained by ReportPortal; the pipeline cannot report it. */
  REPORTPORTAL
}
