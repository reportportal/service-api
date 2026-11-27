package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

/**
 * The CI connection of a pipeline: {@code /v1/project/{key}/pipeline/{id}/ci-connection}.
 * <p>
 * Fixture (pipeline-fill.sql): pipeline 900 "factory-tc-gen" (TEST_CASE_GENERATION) and 902 (no type) in project 1
 * (superadmin_personal) which has one project-wide github-actions integration; pipeline 901 is in project 2, which
 * has no integration.
 */
@Sql("/db/aifactory/pipeline/pipeline-fill.sql")
@Sql(scripts = "/db/aifactory/pipeline/pipeline-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PipelineCiConnectionIntegrationTest extends BaseMvcTest {

  private static final String PIPELINES = "/v1/project/superadmin_personal/pipeline";
  private static final String SECRET = "glptt-very-secret-token";

  private final ObjectMapper mapper = new ObjectMapper();

  private Map<String, Object> generationConnection(String credential, List<String> models,
      List<String> environments) {
    var body = new java.util.LinkedHashMap<String, Object>();
    body.put("repository", "https://git.epam.com/EPM-RPP/rp-tests.git");
    body.put("branch", "develop");
    if (credential != null) {
      body.put("credential", credential);
    }
    body.put("jobs", Map.of("generation", "tc-gen", "fix", "tc-fix"));
    body.put("models", models);
    body.put("environments", environments);
    return body;
  }

  private ResultActions save(long pipelineId, Map<String, Object> body) throws Exception {
    return mockMvc.perform(put(PIPELINES + "/" + pipelineId + "/ci-connection")
        .with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(mapper.writeValueAsString(body)));
  }

  private ResultActions read(String url) throws Exception {
    return mockMvc.perform(get(url).with(token(oAuthHelper.getSuperadminToken())));
  }

  @Test
  void get_WhenNothingIsConfigured_ShouldAnswer200WithNotConnectedAndNoCredential() throws Exception {
    mockMvc.perform(get("/v1/project/default_personal/pipeline/901/ci-connection")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("NOT_CONNECTED"))
        .andExpect(jsonPath("$.canStartJobs").value(false))
        .andExpect(jsonPath("$.credentialSaved").value(false))
        .andExpect(jsonPath("$.provider").value("GITLAB"));
  }

  @Test
  void get_ForAPipelineOnTheProjectIntegration_ShouldBeConnectedAndShowItsRepositoryTokenFree() throws Exception {
    read(PIPELINES + "/902/ci-connection")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("CONNECTED"))
        .andExpect(jsonPath("$.canStartJobs").value(true))
        .andExpect(jsonPath("$.credentialSaved").value(false))
        .andExpect(jsonPath("$.jobs").isEmpty())
        .andExpect(jsonPath("$.environments").isEmpty());
  }

  @Test
  void put_ShouldCreateThePipelinesOwnIntegrationWithoutChangingTheStateOrReturningTheCredential() throws Exception {
    var body = save(900, generationConnection(SECRET, List.of("opus-5", "sonnet-5"), List.of("beta5", "beta6")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.repository").value("https://git.epam.com/EPM-RPP/rp-tests"))
        .andExpect(jsonPath("$.branch").value("develop"))
        .andExpect(jsonPath("$.credentialSaved").value(true))
        .andExpect(jsonPath("$.jobs.generation").value("tc-gen"))
        .andExpect(jsonPath("$.models[0]").value("opus-5"))
        .andExpect(jsonPath("$.environments[1]").value("beta6"))
        .andExpect(jsonPath("$.state").value("NOT_CONNECTED"))
        .andExpect(jsonPath("$.canStartJobs").value(false))
        .andReturn().getResponse().getContentAsString();

    assertFalse(body.contains(SECRET), "the credential must never come back");
  }

  @Test
  void pipeline_AfterTheFirstSave_ShouldShowOnlyTheSafeSummaryAndKeepTheRecordOutOfSettings() throws Exception {
    save(900, generationConnection(SECRET, List.of("opus-5"), List.of("beta5"))).andExpect(status().isOk());

    var body = read(PIPELINES + "/900")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ciConnection.state").value("NOT_CONNECTED"))
        .andExpect(jsonPath("$.ciConnection.canStartJobs").value(false))
        .andExpect(jsonPath("$.settings").doesNotExist())
        .andReturn().getResponse().getContentAsString();

    assertFalse(body.contains(SECRET));
    assertFalse(body.contains("tc-fix"), "jobs are for managers only");
    assertFalse(body.contains("rp-tests"), "the repository is for managers only");
  }

  @Test
  void put_ForOnePipeline_ShouldNotTouchAnotherPipelineThatUsesTheProjectIntegration() throws Exception {
    save(900, generationConnection(SECRET, List.of("opus-5"), List.of("beta5"))).andExpect(status().isOk());

    read(PIPELINES + "/902")
        .andExpect(jsonPath("$.ciConnection.state").value("CONNECTED"))
        .andExpect(jsonPath("$.ciConnection.canStartJobs").value(true));
  }

  @Test
  void put_WithoutACredential_ShouldKeepTheSavedOne() throws Exception {
    save(900, generationConnection(SECRET, List.of("opus-5"), List.of("beta5"))).andExpect(status().isOk());

    save(900, generationConnection(null, List.of("opus-5"), List.of("beta5", "beta6")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.credentialSaved").value(true))
        .andExpect(jsonPath("$.environments.length()").value(2));
  }

  @Test
  void put_WithAnInvalidConnection_ShouldAnswer400() throws Exception {
    var notHttps = generationConnection(SECRET, List.of("opus-5"), List.of("beta5"));
    notHttps.put("repository", "http://git.epam.com/EPM-RPP/rp-tests");
    save(900, notHttps).andExpect(status().isBadRequest());

    var noPath = generationConnection(SECRET, List.of("opus-5"), List.of("beta5"));
    noPath.put("repository", "https://git.epam.com");
    save(900, noPath).andExpect(status().isBadRequest());

    save(900, generationConnection(SECRET, List.of(), List.of("beta5"))).andExpect(status().isBadRequest());
    save(900, generationConnection(SECRET, List.of("opus-5"), List.of("beta5", "beta5")))
        .andExpect(status().isBadRequest());
    save(900, generationConnection(SECRET, List.of("opus-5"), List.of())).andExpect(status().isBadRequest());

    var noFix = generationConnection(SECRET, List.of("opus-5"), List.of("beta5"));
    noFix.put("jobs", Map.of("generation", "tc-gen"));
    save(900, noFix).andExpect(status().isBadRequest());
  }

  @Test
  void deleteCredential_ShouldForgetItAndDisconnect() throws Exception {
    save(900, generationConnection(SECRET, List.of("opus-5"), List.of("beta5"))).andExpect(status().isOk());

    mockMvc.perform(delete(PIPELINES + "/900/ci-connection/credential")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.credentialSaved").value(false))
        .andExpect(jsonPath("$.state").value("NOT_CONNECTED"))
        .andExpect(jsonPath("$.reasonCode").value("NO_CREDENTIAL"))
        .andExpect(jsonPath("$.repository").value("https://git.epam.com/EPM-RPP/rp-tests"));
  }

  @Test
  void test_WithoutACredential_ShouldAnswer200WithNoCredentialAndNotCallGitLab() throws Exception {
    mockMvc.perform(post("/v1/project/default_personal/pipeline/901/ci-connection/test")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("NOT_CONNECTED"))
        .andExpect(jsonPath("$.canStartJobs").value(false))
        .andExpect(jsonPath("$.reasonCode").value("NO_CREDENTIAL"));
  }

  @Test
  void ciConnection_IsNotASettingAClientMayWrite() throws Exception {
    mockMvc.perform(patch(PIPELINES + "/900")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content("{\"settings\": {\"ciConnection\": {\"state\": \"CONNECTED\"}}}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void get_ForAPipelineOfAnotherProject_ShouldAnswer404() throws Exception {
    read(PIPELINES + "/901/ci-connection").andExpect(status().isNotFound());
  }
}
