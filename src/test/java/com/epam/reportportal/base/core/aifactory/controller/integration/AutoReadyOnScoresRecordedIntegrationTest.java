package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ws.BaseMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;

/**
 * A case of a pipeline with Auto-Ready on becomes Ready when its scores are recorded:
 * {@code POST /v1/project/superadmin_personal/tms/test-case/900/generation} with a score of 96 against the threshold
 * 90 answers {@code status: READY}, whenever the producer sends it, also after its Upload stage passed.
 * <p>
 * Fixture: case 900 is linked to stage 900 of pipeline 900 ("factory-tc-gen"); case 901 is not linked to any stage.
 * The test creates the project's quality standard (one criterion, 100 points) and sets the pipeline's Auto-Ready.
 */
@Sql("/db/aifactory/pipeline/pipeline-fill.sql")
@Sql(scripts = "/db/aifactory/pipeline/pipeline-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AutoReadyOnScoresRecordedIntegrationTest extends BaseMvcTest {

  private static final String PROJECT = "/v1/project/superadmin_personal";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * The shared pipeline fixture creates no default version, which scores are recorded against.
   */
  @BeforeEach
  void giveTheFixtureCasesADefaultVersion() {
    jdbcTemplate.update("insert into tms_test_case_version (id, test_case_id, \"name\", is_default, is_draft) "
        + "values (9000, 900, 'Default Version', true, false), (9001, 901, 'Default Version', true, false)");
  }

  @BeforeEach
  void aQualityStandardWithOneCriterion() throws Exception {
    mockMvc.perform(post(PROJECT + "/tms/quality-standard")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content("""
                {"name": "Rubric", "criteria": [{"name": "Overall", "key": "overall", "maxPoints": 100, "sequence": 1}]}
                """))
        .andExpect(status().isCreated());
  }

  private void pipelineAutoReady(String settings) throws Exception {
    mockMvc.perform(patch(PROJECT + "/pipeline/900")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content("{\"settings\": " + settings + "}"))
        .andExpect(status().isOk());
  }

  private ResultActions recordScore(long testCaseId, int score) throws Exception {
    return mockMvc.perform(post(PROJECT + "/tms/test-case/" + testCaseId + "/generation")
            .with(token(oAuthHelper.getSuperadminToken()))
            .contentType(APPLICATION_JSON)
            .content("{\"qualityScores\": [{\"criterionKey\": \"overall\", \"score\": " + score + "}]}"))
        .andExpect(status().isOk());
  }

  @Test
  void generation_WhenTheScoreReachesTheThresholdOfThePipeline_ShouldMakeTheCaseReady() throws Exception {
    pipelineAutoReady("{\"autoReady\": {\"enabled\": true, \"threshold\": 90}}");

    recordScore(900, 96)
        .andExpect(jsonPath("$.origin").value("AI"))
        .andExpect(jsonPath("$.status").value("READY"))
        .andExpect(jsonPath("$.metrics.overallScore").value(96))
        .andExpect(jsonPath("$.madeReadyBy.type").value("AUTO_READY"));
  }

  @Test
  void generation_WhenTheScoreIsBelowTheThreshold_ShouldLeaveTheCaseDraft() throws Exception {
    pipelineAutoReady("{\"autoReady\": {\"enabled\": true, \"threshold\": 90}}");

    recordScore(900, 85)
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.madeReadyBy").doesNotExist());
  }

  @Test
  void generation_WhenThePipelineHasNoAutoReady_ShouldLeaveTheCaseDraft() throws Exception {
    recordScore(900, 100)
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  void generation_ForACaseNoIterationLists_ShouldLeaveItToAUser() throws Exception {
    pipelineAutoReady("{\"autoReady\": {\"enabled\": true, \"threshold\": 90}}");

    recordScore(901, 100)
        .andExpect(jsonPath("$.origin").value("AI"))
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }
}
