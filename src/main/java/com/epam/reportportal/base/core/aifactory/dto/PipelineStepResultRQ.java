package com.epam.reportportal.base.core.aifactory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A generic pointer to whatever artifact a stage produced (e.g.
 * {@code resultType="LAUNCH", resultRef="<launchId>"}), independent of and
 * additive to {@code testCaseIds} — a stage can both touch many test cases and
 * separately point at one produced artifact of a different kind.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepResultRQ {
  private String resultType;
  private String resultRef;
}
