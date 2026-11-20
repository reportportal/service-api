package com.epam.reportportal.base.core.aifactory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Saves the CI connection of a pipeline. The state is not changed by a save: run "Test connection" after it.
 * <p>
 * Example: {@code {"repository": "https://git.epam.com/EPM-RPP/rp-tests", "branch": "main", "credential": "glptt-...",
 * "jobs": {"generation": "tc-gen", "fix": "tc-fix"}, "models": ["opus-5"], "environments": ["beta5", "beta6"]}}.
 * Leave out {@code credential} to keep the saved one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineCiConnectionRQ {

  @NotBlank
  @Schema(description = "Repository URL; the GitLab project path is taken from it (EPM-RPP/rp-tests)",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String repository;
  @Schema(description = "Git branch the jobs run on; main when left out")
  private String branch;
  @Schema(description = "Trigger credential, write only; leave out to keep the saved one")
  private String credential;
  @NotNull
  @NotEmpty
  @Schema(description = "Job names by purpose: generation and fix for Test case generation, automation for Test "
      + "automation", requiredMode = Schema.RequiredMode.REQUIRED)
  private Map<String, String> jobs;
  @Schema(description = "Models a run may use, required for Test case generation; the first is the default")
  private List<String> models;
  @NotNull
  @NotEmpty
  @Schema(description = "Environments the autotests may run against, no duplicates; the first is the default",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<String> environments;
}
