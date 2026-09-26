package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Re-triggers a stage's agent job via the GitHub Actions
 * <a href="https://docs.github.com/en/rest/actions/workflows#create-a-workflow-dispatch-event">
 * workflow dispatch</a> API. {@code stage.ciRepo} is {@code "owner/repo"},
 * {@code stage.ciWorkflowRef} is the workflow file name (e.g. {@code ai-pipeline.yml}).
 */
@Component
@RequiredArgsConstructor
public class GitHubActionsCiConnector extends AbstractCiTriggerConnector {

  private static final String TOKEN_PARAM = "token";
  private static final String GIT_REF_PARAM = "ref";
  private static final String DEFAULT_GIT_REF = "main";

  private final CiHttpClient ciHttpClient;
  private final ObjectMapper objectMapper;

  @Override
  public CiProvider getSupportedProvider() {
    return CiProvider.GITHUB_ACTIONS;
  }

  @Override
  public String triggerStageRerun(Integration integration, PipelineStage stage) {
    if (stage.getCiRepo() == null || stage.getCiWorkflowRef() == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage is missing its CI repo/workflow reference");
    }
    var token = requiredParam(integration, TOKEN_PARAM);
    var gitRef = stringParam(integration, GIT_REF_PARAM);

    var workflowFile = URLEncoder.encode(stage.getCiWorkflowRef(), StandardCharsets.UTF_8);
    var url = "https://api.github.com/repos/" + stage.getCiRepo() + "/actions/workflows/" + workflowFile + "/dispatches";

    ObjectNode body = objectMapper.createObjectNode();
    body.put("ref", gitRef == null || gitRef.isBlank() ? DEFAULT_GIT_REF : gitRef);
    ObjectNode inputs = body.putObject("inputs");
    inputs.put("pipeline_stage_id", String.valueOf(stage.getId()));
    if (stage.getCiJobId() != null) {
      inputs.put("rerun_job_id", stage.getCiJobId());
    }

    ciHttpClient.postForStatus(url, Map.of(
        "Authorization", "Bearer " + token,
        "Accept", "application/vnd.github+json",
        "X-GitHub-Api-Version", "2022-11-28"
    ), body.toString());

    return "https://github.com/" + stage.getCiRepo() + "/actions/workflows/" + workflowFile;
  }
}
