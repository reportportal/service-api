package com.epam.reportportal.base.core.aifactory.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Where and how a stage ran, as far as ReportPortal needs to know to show a link to the run and to run the
 * stage again. ReportPortal does not interpret {@code retryParameters}; only the retry handler registered
 * for {@code executorType} does.
 * <p>
 * Example, a stage that ran as a GitHub Actions job:
 * <pre>
 * {
 *   "executorType": "GITHUB_ACTIONS",
 *   "externalRunId": "78911",
 *   "runUrl": "https://github.com/org/repo/actions/runs/123456/job/78911",
 *   "retryable": true,
 *   "retryParameters": {"repo": "org/repo", "workflowRef": "ai-pipeline.yml"}
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PipelineExecution implements Serializable {

  @NotBlank
  @Size(max = 64)
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
      description = "Kind of system that ran the stage, e.g. GITHUB_ACTIONS or GITLAB_CI. "
          + "It selects the handler that can run the stage again")
  private String executorType;

  @Size(max = 128)
  @Schema(description = "Id of this run of the stage in the system that ran it, e.g. the CI job id \"78911\". "
      + "A finished stage reported again with a different one is a new attempt; with the same one it just "
      + "updates the current attempt")
  private String externalRunId;

  @Size(max = 1024)
  @Schema(description = "Page of this run, shown as a link of the stage, "
      + "e.g. \"https://github.com/org/repo/actions/runs/123456/job/78911\"")
  private String runUrl;

  @Schema(description = "Whether the stage can be run again through the retry handler of its executor type")
  private boolean retryable;

  @Size(max = 20)
  @Schema(description = "Whatever the retry handler of this executor type needs to run the stage again. "
      + "Example for GITHUB_ACTIONS: {\"repo\": \"org/repo\", \"workflowRef\": \"ai-pipeline.yml\"}")
  private Map<String, String> retryParameters;
}
