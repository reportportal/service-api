package com.epam.reportportal.base.core.aifactory.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class GitHubActionsCiConnectorTest {

  private final CiHttpClient ciHttpClient = mock(CiHttpClient.class);
  private final GitHubActionsCiConnector sut = new GitHubActionsCiConnector(ciHttpClient, new ObjectMapper());

  private PipelineStage stage;

  @BeforeEach
  void setUp() {
    stage = new PipelineStage();
    stage.setId(50L);
    stage.setCiRepo("org/repo");
    stage.setCiWorkflowRef("ai-pipeline.yml");
    stage.setCiJobId("78911");
  }

  private Integration integrationWithParams(Map<String, Object> params) {
    var integration = new Integration();
    integration.setParams(new IntegrationParams(params));
    return integration;
  }

  @Test
  void getSupportedProvider_ShouldBeGitHubActions() {
    assertEquals(CiProvider.GITHUB_ACTIONS, sut.getSupportedProvider());
  }

  @Test
  void triggerStageRerun_ShouldDispatchWorkflowWithTokenAndReturnActionsUrl() {
    var integration = integrationWithParams(Map.of("token", "gh-token", "ref", "release-branch", "repo", "org/repo"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    var result = sut.triggerStageRerun(integration, stage);

    assertEquals("https://github.com/org/repo/actions/workflows/ai-pipeline.yml", result);

    var urlCaptor = ArgumentCaptor.forClass(String.class);
    var headersCaptor = ArgumentCaptor.forClass(Map.class);
    var bodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForStatus(urlCaptor.capture(), headersCaptor.capture(), bodyCaptor.capture());

    assertEquals("https://api.github.com/repos/org/repo/actions/workflows/ai-pipeline.yml/dispatches",
        urlCaptor.getValue());
    assertEquals("Bearer gh-token", headersCaptor.getValue().get("Authorization"));
    assertTrue(bodyCaptor.getValue().contains("\"ref\":\"release-branch\""));
    assertTrue(bodyCaptor.getValue().contains("\"pipeline_stage_id\":\"50\""));
    assertTrue(bodyCaptor.getValue().contains("\"rerun_job_id\":\"78911\""));
  }

  @Test
  void triggerStageRerun_WithoutGitRefParam_ShouldDefaultToMain() {
    var integration = integrationWithParams(Map.of("token", "gh-token", "repo", "org/repo"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    sut.triggerStageRerun(integration, stage);

    var bodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForStatus(anyString(), anyMap(), bodyCaptor.capture());
    assertTrue(bodyCaptor.getValue().contains("\"ref\":\"main\""));
  }

  @Test
  void triggerStageRerun_WhenMissingCiRepoOrWorkflowRef_ShouldThrowBadRequestWithoutCallingHttpClient() {
    stage.setCiWorkflowRef(null);
    var integration = integrationWithParams(Map.of("token", "gh-token", "repo", "org/repo"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void triggerStageRerun_WhenIntegrationMissingToken_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("repo", "org/repo"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void triggerStageRerun_WhenIntegrationMissingRepo_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("token", "gh-token"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void triggerStageRerun_WhenStageRepoDoesNotMatchIntegrationRepo_ShouldThrowBadRequestWithoutCallingHttpClient() {
    var integration = integrationWithParams(Map.of("token", "gh-token", "repo", "org/other-repo"));

    assertThrows(ReportPortalException.class, () -> sut.triggerStageRerun(integration, stage));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }
}
