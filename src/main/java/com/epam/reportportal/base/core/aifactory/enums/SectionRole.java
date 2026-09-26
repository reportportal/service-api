package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Which part of the stage view a section belongs to.
 * <p>
 * Example: on a Grade stage the table of graded test cases is a {@link #RESULT} section (the main panel), the
 * token usage per model is a {@link #DETAILS} section (the side panel next to the stage's duration, CI job and
 * cost).
 */
@Schema(description = "RESULT - what the stage produced, shown in the main panel (graded cases, upload report); "
    + "DETAILS - facts about how the stage ran, shown beside it (token usage per model)")
public enum SectionRole {
  RESULT,
  DETAILS
}
