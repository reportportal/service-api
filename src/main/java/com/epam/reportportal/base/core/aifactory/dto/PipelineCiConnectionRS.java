package com.epam.reportportal.base.core.aifactory.dto;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;
import com.epam.reportportal.base.core.aifactory.enums.CiConnectionState;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The full CI connection of a pipeline, for Organization Managers and Administrators. The credential is never
 * part of it.
 * <p>
 * Example: {@code {"provider": "GITLAB", "repository": "https://git.epam.com/EPM-RPP/rp-tests", "branch": "main",
 * "credentialSaved": true, "jobs": {"generation": "tc-gen", "fix": "tc-fix"}, "models": ["opus-5"],
 * "environments": ["beta5"], "state": "CONNECTED", "canStartJobs": true}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineCiConnectionRS {

  @Schema(description = "The CI system; GITLAB is the only one for now")
  private String provider;
  @Schema(description = "Repository URL, e.g. https://git.epam.com/EPM-RPP/rp-tests; empty when nothing is configured")
  private String repository;
  @Schema(description = "Git branch the jobs run on, main when not set")
  private String branch;
  @Schema(description = "True when a trigger credential is saved; the credential itself is never returned")
  private boolean credentialSaved;
  @Schema(description = "Job names by purpose: generation and fix for Test case generation, automation for Test automation")
  private Map<String, String> jobs;
  @Schema(description = "Models a run may use (generation only); the first is the default")
  private List<String> models;
  @Schema(description = "Environments the autotests may run against; the first is the default")
  private List<String> environments;
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private CiConnectionState state;
  @Schema(description = "True only when state is CONNECTED: Push to agent, Retry, Re-run and Automate are allowed",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean canStartJobs;
  private CiConnectionReasonCode reasonCode;
  private String reason;
  private Instant checkedAt;
}
