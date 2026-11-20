package com.epam.reportportal.base.core.aifactory.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.enums.CiConnectionReasonCode;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

class GitLabCiConnectorTest {

  private final CiHttpClient ciHttpClient = mock(CiHttpClient.class);
  private final GitLabCiConnector sut = new GitLabCiConnector(ciHttpClient, JsonMapper.builder().build());

  private PipelineStage stage;

  @BeforeEach
  void setUp() {
    stage = new PipelineStage();
    stage.setId(50L);
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder()
        .executorType("GITLAB_CI").externalRunId("78911")
        .retryParameters(new HashMap<>(Map.of("repo", "42", "workflowRef", "main"))).build()));
  }

  private Integration integrationWithParams(Map<String, Object> params) {
    var integration = new Integration();
    integration.setParams(new IntegrationParams(params));
    return integration;
  }

  @Test
  void getSupportedExecutorType_ShouldBeGitLabCiAndUseItsIntegrationType() {
    assertEquals("GITLAB_CI", sut.getSupportedExecutorType());
    assertEquals("gitlab-ci", sut.getIntegrationTypeName());
  }

  @Test
  void triggerStageRerun_ShouldTriggerPipelineAndReturnWebUrlFromResponse() {
    var integration = integrationWithParams(
        Map.of("triggerToken", "gl-token", "url", "https://gitlab.internal", "projectId", "42"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString()))
        .thenReturn("{\"web_url\": \"https://gitlab.internal/org/repo/-/pipelines/999\"}");

    var result = sut.triggerStageRerun(integration, stage);

    assertEquals("https://gitlab.internal/org/repo/-/pipelines/999", result);

    var urlCaptor = ArgumentCaptor.forClass(String.class);
    var bodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForStatus(urlCaptor.capture(), anyMap(), bodyCaptor.capture());

    assertEquals("https://gitlab.internal/api/v4/projects/42/trigger/pipeline", urlCaptor.getValue());
    assertTrue(bodyCaptor.getValue().contains("\"token\":\"gl-token\""));
    assertTrue(bodyCaptor.getValue().contains("\"ref\":\"main\""));
    assertTrue(bodyCaptor.getValue().contains("\"PIPELINE_STAGE_ID\":\"50\""));
    assertTrue(bodyCaptor.getValue().contains("\"RERUN_JOB_ID\":\"78911\""));
  }

  @Test
  void triggerStageRerun_WhenResponseHasNoWebUrl_ShouldReturnFallbackPipelinesUrl() {
    var integration = integrationWithParams(
        Map.of("triggerToken", "gl-token", "url", "https://gitlab.internal", "projectId", "42"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("{}");

    var result = sut.triggerStageRerun(integration, stage);

    assertEquals("https://gitlab.internal/api/v4/projects/42/pipelines", result);
  }

  @Test
  void triggerStageRerun_WhenResponseIsNotJson_ShouldReturnFallbackPipelinesUrl() {
    var integration = integrationWithParams(
        Map.of("triggerToken", "gl-token", "url", "https://gitlab.internal", "projectId", "42"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("not-json");

    var result = sut.triggerStageRerun(integration, stage);

    assertEquals("https://gitlab.internal/api/v4/projects/42/pipelines", result);
  }

  @Test
  void triggerStageRerun_WithoutBaseUrlParam_ShouldDefaultToGitlabCom() {
    var integration = integrationWithParams(Map.of("triggerToken", "gl-token", "projectId", "42"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("{}");

    var result = sut.triggerStageRerun(integration, stage);

    assertEquals("https://gitlab.com/api/v4/projects/42/pipelines", result);
  }

  @Test
  void triggerStageRerun_WhenMissingCiRepoOrWorkflowRef_ShouldThrowBadRequestWithoutCallingHttpClient() {
    stage.getExecution().getContent().getRetryParameters().remove("repo");
    var integration = integrationWithParams(Map.of("triggerToken", "gl-token", "projectId", "42"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void triggerStageRerun_WhenIntegrationMissingToken_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("projectId", "42"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void triggerStageRerun_WhenIntegrationMissingProjectId_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("triggerToken", "gl-token"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  private Integration pathIntegration() {
    return integrationWithParams(
        Map.of("triggerToken", "gl-token", "url", "https://git.epam.com", "projectId", "EPM-RPP/rp-tests"));
  }

  @Test
  void checkConnection_WhenGitLabAnswersReferenceNotFound_ShouldBeConnectedAndAskWithARefThatDoesNotExist() {
    when(ciHttpClient.postForResponse(anyString(), anyMap(), anyString()))
        .thenReturn(new CiHttpResponse(400, "{\"message\":{\"base\":[\"Reference not found\"]}}"));

    var check = sut.checkConnection(pathIntegration());

    assertTrue(check.connected());
    var url = ArgumentCaptor.forClass(String.class);
    var body = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForResponse(url.capture(), anyMap(), body.capture());
    assertEquals("https://git.epam.com/api/v4/projects/EPM-RPP%2Frp-tests/trigger/pipeline", url.getValue());
    assertTrue(body.getValue().contains("\"ref\":\"rp-connection-check-"));
  }

  @Test
  void checkConnection_WhenGitLabAnswers404_ShouldSayRepositoryOrCredentialInvalid() {
    when(ciHttpClient.postForResponse(anyString(), anyMap(), anyString()))
        .thenReturn(new CiHttpResponse(404, "{\"message\":\"404 Not Found\"}"));

    var check = sut.checkConnection(pathIntegration());

    assertEquals(false, check.connected());
    assertEquals(CiConnectionReasonCode.REPOSITORY_OR_CREDENTIAL_INVALID, check.reasonCode());
  }

  @Test
  void checkConnection_WhenGitLabAnswersAServerError_ShouldSayUnreachable() {
    when(ciHttpClient.postForResponse(anyString(), anyMap(), anyString()))
        .thenReturn(new CiHttpResponse(502, "Bad Gateway"));

    assertEquals(CiConnectionReasonCode.UNREACHABLE, sut.checkConnection(pathIntegration()).reasonCode());
  }

  @Test
  void checkConnection_WhenThereIsNoAnswer_ShouldSayUnreachable() {
    when(ciHttpClient.postForResponse(anyString(), anyMap(), anyString()))
        .thenThrow(new CiUnreachableException("No answer from the CI: timeout"));

    var check = sut.checkConnection(pathIntegration());

    assertEquals(CiConnectionReasonCode.UNREACHABLE, check.reasonCode());
    assertTrue(check.reason().contains("timeout"));
  }

  @Test
  void checkConnection_WhenAnythingElseComesBack_ShouldSayUnexpectedWithStatusAndNeverRepeatTheToken() {
    when(ciHttpClient.postForResponse(anyString(), anyMap(), anyString()))
        .thenReturn(new CiHttpResponse(400, "bad request for gl-token"));

    var check = sut.checkConnection(pathIntegration());

    assertEquals(CiConnectionReasonCode.UNEXPECTED_RESPONSE, check.reasonCode());
    assertTrue(check.reason().contains("HTTP 400"));
    assertEquals(false, check.reason().contains("gl-token"));
  }

  @Test
  void checkConnection_WithoutACredential_ShouldNotCallGitLab() {
    var check = sut.checkConnection(integrationWithParams(Map.of("projectId", "EPM-RPP/rp-tests")));

    assertEquals(CiConnectionReasonCode.NO_CREDENTIAL, check.reasonCode());
    verify(ciHttpClient, never()).postForResponse(anyString(), anyMap(), anyString());
  }

  @Test
  void triggerStageRerun_WhenStageProjectDoesNotMatchIntegrationProjectId_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("triggerToken", "gl-token", "projectId", "99"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }
}
