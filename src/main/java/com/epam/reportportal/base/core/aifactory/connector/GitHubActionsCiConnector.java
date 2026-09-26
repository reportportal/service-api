package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

/**
 * Re-triggers a stage's agent job via the GitHub Actions
 * <a href="https://docs.github.com/en/rest/actions/workflows#create-a-workflow-dispatch-event">
 * workflow dispatch</a> API. The retry parameter {@code repo} is {@code "owner/repo"},
 * {@code workflowRef} is the workflow file name (e.g. {@code ai-pipeline.yml}).
 */
@Component
@RequiredArgsConstructor
public class GitHubActionsCiConnector extends AbstractCiTriggerConnector {

  private static final String TOKEN_PARAM = "token";
  private static final String GIT_REF_PARAM = "ref";
  private static final String REPO_PARAM = "repo";
  private static final String DEFAULT_GIT_REF = "main";
  private static final String WORKFLOW_PARAM = "workflowRef";
  private static final String FIX_WORKFLOW_PARAM = "fixWorkflowRef";

  private final CiHttpClient ciHttpClient;
  private final ObjectMapper objectMapper;

  @Override
  public String getSupportedExecutorType() {
    return "GITHUB_ACTIONS";
  }

  @Override
  public String getIntegrationTypeName() {
    return "github-actions";
  }

  @Override
  public String triggerStageRerun(Integration integration, PipelineStage stage) {
    var repo = retryParameter(stage, REPO_PARAM);
    var workflowRef = retryParameter(stage, WORKFLOW_PARAM);
    if (repo == null || workflowRef == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage is missing its CI repo/workflow reference");
    }
    var token = requiredParam(integration, TOKEN_PARAM);
    // The stage's repo is client-supplied at ingest time and must never steer which repo the
    // integration's token is used against - it's only accepted here once it's confirmed to be
    // the exact repo this integration was configured for.
    var configuredRepo = requiredParam(integration, REPO_PARAM);
    if (!configuredRepo.equals(repo)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Stage CI repo '" + repo + "' does not match the repo configured for this project's GitHub Actions integration");
    }
    var gitRef = stringParam(integration, GIT_REF_PARAM);

    var workflowFile = UriUtils.encodePathSegment(workflowRef, StandardCharsets.UTF_8);
    var url = "https://api.github.com/repos/" + repo + "/actions/workflows/" + workflowFile + "/dispatches";

    ObjectNode body = objectMapper.createObjectNode();
    body.put("ref", gitRef == null || gitRef.isBlank() ? DEFAULT_GIT_REF : gitRef);
    ObjectNode inputs = body.putObject("inputs");
    inputs.put("pipeline_stage_id", String.valueOf(stage.getId()));
    if (externalRunId(stage) != null) {
      inputs.put("rerun_job_id", externalRunId(stage));
    }

    ciHttpClient.postForStatus(url, Map.of(
        "Authorization", "Bearer " + token,
        "Accept", "application/vnd.github+json",
        "X-GitHub-Api-Version", "2022-11-28"
    ), body.toString());

    return "https://github.com/" + repo + "/actions/workflows/" + workflowFile;
  }

  @Override
  public String triggerRun(Integration integration, CiRunRequest request) {
    var token = requiredParam(integration, TOKEN_PARAM);
    // same rule as for a rerun: the repo named by a caller never steers where the token of the integration is used
    var configuredRepo = requiredParam(integration, REPO_PARAM);
    if (request.repo() == null || !configuredRepo.equals(request.repo())) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "CI repo '" + request.repo() + "' does not match the repo configured for this project's GitHub Actions integration");
    }
    var configuredWorkflow = stringParam(integration, FIX_WORKFLOW_PARAM);
    var workflowRef = configuredWorkflow == null || configuredWorkflow.isBlank() ? request.workflowRef() : configuredWorkflow;
    if (workflowRef == null || workflowRef.isBlank()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "No workflow to start: set '" + FIX_WORKFLOW_PARAM + "' on the GitHub Actions integration");
    }
    var gitRef = stringParam(integration, GIT_REF_PARAM);

    var workflowFile = UriUtils.encodePathSegment(workflowRef, StandardCharsets.UTF_8);
    var url = "https://api.github.com/repos/" + configuredRepo + "/actions/workflows/" + workflowFile + "/dispatches";

    ObjectNode body = objectMapper.createObjectNode();
    body.put("ref", gitRef == null || gitRef.isBlank() ? DEFAULT_GIT_REF : gitRef);
    ObjectNode inputs = body.putObject("inputs");
    request.inputs().forEach(inputs::put);

    ciHttpClient.postForStatus(url, Map.of(
        "Authorization", "Bearer " + token,
        "Accept", "application/vnd.github+json",
        "X-GitHub-Api-Version", "2022-11-28"
    ), body.toString());

    return "https://github.com/" + configuredRepo + "/actions/workflows/" + workflowFile;
  }
}
