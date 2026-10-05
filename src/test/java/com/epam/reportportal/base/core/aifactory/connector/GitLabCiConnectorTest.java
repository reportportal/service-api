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

import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GitLabCiConnectorTest {

  private final CiHttpClient ciHttpClient = mock(CiHttpClient.class);
  private final GitLabCiConnector sut = new GitLabCiConnector(ciHttpClient, new ObjectMapper());

  private PipelineStage stage;

  @BeforeEach
  void setUp() {
    stage = new PipelineStage();
    stage.setId(50L);
    stage.setCiRepo("42");
    stage.setCiWorkflowRef("main");
    stage.setCiJobId("78911");
  }

  private Integration integrationWithParams(Map<String, Object> params) {
    var integration = new Integration();
    integration.setParams(new IntegrationParams(params));
    return integration;
  }

  @Test
  void getSupportedProvider_ShouldBeGitLabCi() {
    assertEquals(CiProvider.GITLAB_CI, sut.getSupportedProvider());
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
    stage.setCiRepo(null);
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

  @Test
  void triggerStageRerun_WhenStageProjectDoesNotMatchIntegrationProjectId_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("triggerToken", "gl-token", "projectId", "99"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }
}
