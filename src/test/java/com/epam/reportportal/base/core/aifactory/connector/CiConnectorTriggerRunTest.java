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

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Starting a brand-new run (the fix job of a test case), as opposed to running a stage again.
 */
class CiConnectorTriggerRunTest {

  private final CiHttpClient ciHttpClient = mock(CiHttpClient.class);
  private final GitHubActionsCiConnector gitHub = new GitHubActionsCiConnector(ciHttpClient, new ObjectMapper());
  private final GitLabCiConnector gitLab = new GitLabCiConnector(ciHttpClient, new ObjectMapper());

  private Integration integrationWithParams(Map<String, Object> params) {
    var integration = new Integration();
    integration.setParams(new IntegrationParams(params));
    return integration;
  }

  private CiRunRequest fixRunRequest(String repo, String workflowRef) {
    var inputs = new LinkedHashMap<String, String>();
    inputs.put("test_case_id", "103");
    inputs.put("push_id", "7");
    return new CiRunRequest(repo, workflowRef, inputs);
  }

  @Test
  void gitHubTriggerRun_ShouldDispatchTheWorkflowWithTheInputsAndReturnTheWorkflowPage() {
    var integration = integrationWithParams(Map.of("token", "gh-token", "repo", "org/repo"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    var url = gitHub.triggerRun(integration, fixRunRequest("org/repo", "fix-test-case.yml"));

    assertEquals("https://github.com/org/repo/actions/workflows/fix-test-case.yml", url);
    var urlCaptor = ArgumentCaptor.forClass(String.class);
    var headersCaptor = ArgumentCaptor.forClass(Map.class);
    var bodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForStatus(urlCaptor.capture(), headersCaptor.capture(), bodyCaptor.capture());
    assertEquals("https://api.github.com/repos/org/repo/actions/workflows/fix-test-case.yml/dispatches",
        urlCaptor.getValue());
    assertEquals("Bearer gh-token", headersCaptor.getValue().get("Authorization"));
    assertTrue(bodyCaptor.getValue().contains("\"ref\":\"main\""));
    assertTrue(bodyCaptor.getValue().contains("\"test_case_id\":\"103\""));
    assertTrue(bodyCaptor.getValue().contains("\"push_id\":\"7\""));
  }

  @Test
  void gitHubTriggerRun_WhenTheIntegrationNamesTheFixWorkflow_ShouldUseItInsteadOfTheRequestedOne() {
    var integration = integrationWithParams(
        Map.of("token", "gh-token", "repo", "org/repo", "fixWorkflowRef", "fix.yml"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    var url = gitHub.triggerRun(integration, fixRunRequest("org/repo", "ai-pipeline.yml"));

    assertEquals("https://github.com/org/repo/actions/workflows/fix.yml", url);
  }

  @Test
  void gitHubTriggerRun_WhenTheRepoIsNotTheConfiguredOne_ShouldRefuseWithoutCallingGitHub() {
    var integration = integrationWithParams(Map.of("token", "gh-token", "repo", "org/repo"));

    assertThrows(ReportPortalException.class,
        () -> gitHub.triggerRun(integration, fixRunRequest("someone-else/repo", "fix.yml")));

    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void gitHubTriggerRun_WithoutAnyWorkflow_ShouldRefuseWithoutCallingGitHub() {
    var integration = integrationWithParams(Map.of("token", "gh-token", "repo", "org/repo"));

    assertThrows(ReportPortalException.class, () -> gitHub.triggerRun(integration, fixRunRequest("org/repo", null)));

    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void gitLabTriggerRun_ShouldTriggerThePipelineWithTheInputsAsVariablesAndReturnItsPage() {
    var integration = integrationWithParams(Map.of("triggerToken", "glptt", "projectId", "42"));
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString()))
        .thenReturn("{\"web_url\":\"https://gitlab.com/org/repo/-/pipelines/9\"}");

    var url = gitLab.triggerRun(integration, fixRunRequest("42", "main"));

    assertEquals("https://gitlab.com/org/repo/-/pipelines/9", url);
    var urlCaptor = ArgumentCaptor.forClass(String.class);
    var bodyCaptor = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForStatus(urlCaptor.capture(), anyMap(), bodyCaptor.capture());
    assertEquals("https://gitlab.com/api/v4/projects/42/trigger/pipeline", urlCaptor.getValue());
    assertTrue(bodyCaptor.getValue().contains("\"ref\":\"main\""));
    assertTrue(bodyCaptor.getValue().contains("\"TEST_CASE_ID\":\"103\""));
    assertTrue(bodyCaptor.getValue().contains("\"PUSH_ID\":\"7\""));
  }

  @Test
  void gitLabTriggerRun_WhenTheProjectIsNotTheConfiguredOne_ShouldRefuseWithoutCallingGitLab() {
    var integration = integrationWithParams(Map.of("triggerToken", "glptt", "projectId", "42"));

    assertThrows(ReportPortalException.class, () -> gitLab.triggerRun(integration, fixRunRequest("43", "main")));

    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }
}
