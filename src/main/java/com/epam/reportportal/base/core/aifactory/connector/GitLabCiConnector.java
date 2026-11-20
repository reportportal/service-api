package com.epam.reportportal.base.core.aifactory.connector;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Re-triggers a stage's agent job via the GitLab
 * <a href="https://docs.gitlab.com/ee/ci/triggers/">pipeline trigger</a> API.
 * the retry parameter {@code repo} holds the GitLab project id, {@code workflowRef}
 * the git ref (branch/tag) the pipeline should run on. A run that is not tied to a stage (the fix job) takes its
 * ref from the request, else from the integration's optional {@code ref} parameter, else {@code main}.
 */
@Component
@RequiredArgsConstructor
public class GitLabCiConnector extends AbstractCiTriggerConnector {

  private static final String BASE_URL_PARAM = "url";
  private static final String TOKEN_PARAM = "triggerToken";
  private static final String PROJECT_ID_PARAM = "projectId";
  private static final String DEFAULT_BASE_URL = "https://gitlab.com";
  private static final String REPO_PARAM = "repo";
  private static final String REF_PARAM = "workflowRef";
  private static final String INTEGRATION_REF_PARAM = "ref";
  private static final String DEFAULT_REF = "main";
  /** Prefix of the git ref used by the connection test; no branch is ever called like this. */
  private static final String CHECK_REF_PREFIX = "rp-connection-check-";
  private static final String API_PROJECTS_PATH = "/api/v4/projects/";
  private static final String TRIGGER_PATH = "/trigger/pipeline";
  private static final String TOKEN_FIELD = "token";

  private final CiHttpClient ciHttpClient;
  private final ObjectMapper objectMapper;

  @Override
  public String getSupportedExecutorType() {
    return "GITLAB_CI";
  }

  @Override
  public String getIntegrationTypeName() {
    return "gitlab-ci";
  }

  @Override
  public String triggerStageRerun(Integration integration, PipelineStage stage) {
    var repo = retryParameter(stage, REPO_PARAM);
    var ref = retryParameter(stage, REF_PARAM);
    if (repo == null || ref == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage is missing its CI project/ref reference");
    }
    var token = requiredParam(integration, TOKEN_PARAM);
    // The stage's project id is client-supplied at ingest time and must never steer which
    // GitLab project the integration's trigger token is used against - it's only accepted here
    // once it's confirmed to be the exact project this integration was configured for.
    var configuredProjectId = requiredParam(integration, PROJECT_ID_PARAM);
    if (!configuredProjectId.equals(repo)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Stage CI project '" + repo + "' does not match the project configured for this project's GitLab CI integration");
    }
    var baseUrl = stringParam(integration, BASE_URL_PARAM);
    var effectiveBaseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;

    var projectId = UriUtils.encodePathSegment(repo, StandardCharsets.UTF_8);
    var url = effectiveBaseUrl + API_PROJECTS_PATH + projectId + TRIGGER_PATH;

    ObjectNode body = objectMapper.createObjectNode();
    body.put(TOKEN_FIELD, token);
    body.put("ref", ref);
    ObjectNode variables = body.putObject("variables");
    variables.put("PIPELINE_STAGE_ID", String.valueOf(stage.getId()));
    if (externalRunId(stage) != null) {
      variables.put("RERUN_JOB_ID", externalRunId(stage));
    }

    var responseBody = ciHttpClient.postForStatus(url, Collections.emptyMap(), body.toString());
    var fallbackUrl = effectiveBaseUrl + API_PROJECTS_PATH + projectId + "/pipelines";
    return extractPipelineUrl(responseBody, fallbackUrl);
  }

  /**
   * The git ref is the one of the request; when it has none, the integration's {@code ref}; when that is not
   * set either, {@code main}.
   * <p>
   * Example: a stage reported {@code workflowRef: "release"} - the run goes to {@code release}. A stage reported
   * none and the integration has {@code ref: "develop"} - it goes to {@code develop}. Neither - {@code main}.
   */
  @Override
  public CiRunRequest resolveRun(Integration integration, CiRunRequest request) {
    requiredParam(integration, TOKEN_PARAM);
    var configuredProjectId = requiredParam(integration, PROJECT_ID_PARAM);
    if (request.repo() == null || !configuredProjectId.equals(request.repo())) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "CI project '" + request.repo()
              + "' does not match the project configured for this project's GitLab CI integration");
    }
    return new CiRunRequest(request.repo(), gitRef(integration, request.workflowRef()), request.inputs());
  }

  private String gitRef(Integration integration, String requestedRef) {
    if (requestedRef != null && !requestedRef.isBlank()) {
      return requestedRef;
    }
    var configuredRef = stringParam(integration, INTEGRATION_REF_PARAM);
    return configuredRef == null || configuredRef.isBlank() ? DEFAULT_REF : configuredRef;
  }

  @Override
  public String triggerRun(Integration integration, CiRunRequest requested) {
    var request = resolveRun(integration, requested);
    var token = requiredParam(integration, TOKEN_PARAM);
    var configuredProjectId = requiredParam(integration, PROJECT_ID_PARAM);
    var baseUrl = stringParam(integration, BASE_URL_PARAM);
    var effectiveBaseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
    var projectId = UriUtils.encodePathSegment(configuredProjectId, StandardCharsets.UTF_8);
    var url = effectiveBaseUrl + API_PROJECTS_PATH + projectId + TRIGGER_PATH;

    ObjectNode body = objectMapper.createObjectNode();
    body.put(TOKEN_FIELD, token);
    body.put("ref", request.workflowRef());
    ObjectNode variables = body.putObject("variables");
    request.inputs().forEach((name, value) -> variables.put(name.toUpperCase(), value));

    var responseBody = ciHttpClient.postForStatus(url, Collections.emptyMap(), body.toString());
    return extractPipelineUrl(responseBody, effectiveBaseUrl + API_PROJECTS_PATH + projectId + "/pipelines");
  }

  /**
   * A trigger token cannot read the project or its jobs, so the connection is checked by asking GitLab to start a
   * pipeline on a git ref that does not exist. GitLab checks the token and the project first and creates nothing:
   * <ul>
   *   <li>400 "Reference not found": the token fits the project, connected;</li>
   *   <li>404 (also 401/403): no such project, or the token does not fit it - GitLab answers the same for both;</li>
   *   <li>5xx or no answer: unreachable;</li>
   *   <li>anything else (a 2xx would mean a pipeline was created): unexpected, with status and message.</li>
   * </ul>
   * Example: project {@code EPM-RPP/rp-tests} with a good token -> connected; the same with a wrong token ->
   * {@code REPOSITORY_OR_CREDENTIAL_INVALID}.
   */
  @Override
  public CiConnectionCheck checkConnection(Integration integration) {
    var token = stringParam(integration, TOKEN_PARAM);
    var configuredProject = stringParam(integration, PROJECT_ID_PARAM);
    if (token == null || token.isBlank() || configuredProject == null || configuredProject.isBlank()) {
      return CiConnectionCheck.failed(CiConnectionReasonCode.NO_CREDENTIAL, "No credential or repository is saved");
    }
    var baseUrl = stringParam(integration, BASE_URL_PARAM);
    var effectiveBaseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl;
    var url = effectiveBaseUrl + API_PROJECTS_PATH + UriUtils.encodePathSegment(configuredProject,
        StandardCharsets.UTF_8) + TRIGGER_PATH;

    ObjectNode body = objectMapper.createObjectNode();
    body.put(TOKEN_FIELD, token);
    body.put("ref", CHECK_REF_PREFIX + UUID.randomUUID());

    CiHttpResponse response;
    try {
      response = ciHttpClient.postForResponse(url, Collections.emptyMap(), body.toString());
    } catch (CiUnreachableException e) {
      return CiConnectionCheck.failed(CiConnectionReasonCode.UNREACHABLE, e.getMessage());
    } catch (ReportPortalException e) {
      return CiConnectionCheck.failed(CiConnectionReasonCode.UNEXPECTED_RESPONSE, e.getMessage());
    }

    var status = response.status();
    if (status == 400 && response.body() != null && response.body().contains("Reference not found")) {
      return CiConnectionCheck.ok();
    }
    if (status == 404 || status == 401 || status == 403) {
      return CiConnectionCheck.failed(CiConnectionReasonCode.REPOSITORY_OR_CREDENTIAL_INVALID,
          "GitLab does not know this project or does not accept the credential for it (HTTP " + status + ")");
    }
    if (status >= 500) {
      return CiConnectionCheck.failed(CiConnectionReasonCode.UNREACHABLE, "GitLab answered HTTP " + status);
    }
    return CiConnectionCheck.failed(CiConnectionReasonCode.UNEXPECTED_RESPONSE,
        "GitLab answered HTTP " + status + ": " + shortened(response.body(), token));
  }

  private static String shortened(String text, String secret) {
    if (text == null) {
      return "";
    }
    var withoutSecret = text.replace(secret, "***");
    return withoutSecret.length() <= 200 ? withoutSecret : withoutSecret.substring(0, 200) + "...";
  }

  /**
   * GitLab's trigger-pipeline response body carries the created pipeline's own
   * {@code web_url} — prefer that (a page a human can open) over the raw REST
   * endpoint this connector called.
   */
  private String extractPipelineUrl(String responseBody, String fallbackUrl) {
    try {
      var webUrl = objectMapper.readTree(responseBody).path("web_url").asText(null);
      return webUrl != null && !webUrl.isBlank() ? webUrl : fallbackUrl;
    } catch (JacksonException | IllegalArgumentException e) {
      return fallbackUrl;
    }
  }
}
