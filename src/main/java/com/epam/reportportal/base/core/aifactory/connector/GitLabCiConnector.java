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
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Re-triggers a stage's agent job via the GitLab
 * <a href="https://docs.gitlab.com/ee/ci/triggers/">pipeline trigger</a> API.
 * {@code stage.ciRepo} holds the GitLab project id, {@code stage.ciWorkflowRef}
 * the git ref (branch/tag) the pipeline should run on.
 */
@Component
@RequiredArgsConstructor
public class GitLabCiConnector extends AbstractCiTriggerConnector {

  private static final String BASE_URL_PARAM = "url";
  private static final String TOKEN_PARAM = "triggerToken";
  private static final String DEFAULT_BASE_URL = "https://gitlab.com";

  private final CiHttpClient ciHttpClient;
  private final ObjectMapper objectMapper;

  @Override
  public CiProvider getSupportedProvider() {
    return CiProvider.GITLAB_CI;
  }

  @Override
  public String triggerStageRerun(Integration integration, PipelineStage stage) {
    if (stage.getCiRepo() == null || stage.getCiWorkflowRef() == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage is missing its CI project/ref reference");
    }
    var token = requiredParam(integration, TOKEN_PARAM);
    var baseUrl = stringParam(integration, BASE_URL_PARAM);
    var effectiveBaseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;

    var projectId = URLEncoder.encode(stage.getCiRepo(), StandardCharsets.UTF_8);
    var url = effectiveBaseUrl + "/api/v4/projects/" + projectId + "/trigger/pipeline";

    ObjectNode body = objectMapper.createObjectNode();
    body.put("token", token);
    body.put("ref", stage.getCiWorkflowRef());
    ObjectNode variables = body.putObject("variables");
    variables.put("PIPELINE_STAGE_ID", String.valueOf(stage.getId()));
    if (stage.getCiJobId() != null) {
      variables.put("RERUN_JOB_ID", stage.getCiJobId());
    }

    ciHttpClient.postForStatus(url, Collections.emptyMap(), body.toString());

    return effectiveBaseUrl + "/api/v4/projects/" + projectId + "/pipelines";
  }
}
