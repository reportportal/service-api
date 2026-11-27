package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * The test case list narrowed to one pipeline iteration with {@code filter.eq.iterationId}, e.g.
 * {@code GET /v1/project/superadmin_personal/tms/test-case?filter.eq.iterationId=901}.
 * <p>
 * Fixture: case 900 is linked to stage 900 (iteration 900); the test also links case 900 to the two stages of
 * iteration 901 and case 901 to the second of them.
 */
@Sql("/db/aifactory/pipeline/pipeline-fill.sql")
@Sql(scripts = "/db/aifactory/pipeline/pipeline-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TestCaseIterationFilterIntegrationTest extends BaseMvcTest {

  private static final String TEST_CASES = "/v1/project/superadmin_personal/tms/test-case";

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * The list maps every case with its default version, which the shared pipeline fixture does not create.
   */
  @BeforeEach
  void giveTheFixtureCasesADefaultVersion() {
    jdbcTemplate.update("insert into tms_test_case_version (id, test_case_id, \"name\", is_default, is_draft) "
        + "values (9000, 900, 'Default Version', true, false), (9001, 901, 'Default Version', true, false)");
  }

  private void linkCaseToStage(long testCaseId, long stageId) {
    jdbcTemplate.update("insert into pipeline_stage_test_case (stage_id, test_case_id) values (?, ?)", stageId,
        testCaseId);
  }

  private ResultActions listFor(String query) throws Exception {
    return mockMvc.perform(get(TEST_CASES + query).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  @Test
  void list_ByIteration_ShouldReturnOnlyTheCasesLinkedToIt() throws Exception {
    listFor("?filter.eq.iterationId=900")
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(900));
  }

  @Test
  void list_ByIteration_WhenACaseIsLinkedThroughSeveralStages_ShouldListItOnce() throws Exception {
    linkCaseToStage(900, 901);
    linkCaseToStage(900, 902);
    linkCaseToStage(901, 902);

    listFor("?filter.eq.iterationId=901")
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(2))
        .andExpect(jsonPath("$.content[?(@.id==900)]").isNotEmpty())
        .andExpect(jsonPath("$.content[?(@.id==901)]").isNotEmpty());
  }

  @Test
  void list_ByIteration_ShouldKeepACaseInEveryIterationItIsLinkedTo() throws Exception {
    // a case fixed in iteration 901 stays in iteration 900 that made it
    linkCaseToStage(900, 901);

    listFor("?filter.eq.iterationId=900")
        .andExpect(jsonPath("$.content[?(@.id==900)]").isNotEmpty());
    listFor("?filter.eq.iterationId=901")
        .andExpect(jsonPath("$.content[?(@.id==900)]").isNotEmpty());
  }

  @Test
  void list_ByIteration_WithAnotherFilter_ShouldApplyBoth() throws Exception {
    linkCaseToStage(900, 901);
    linkCaseToStage(901, 901);

    listFor("?filter.eq.iterationId=901&filter.eq.displayId=TC-901")
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(901));
  }

  @Test
  void list_ByIteration_WithoutLinkedCases_ShouldBeEmpty() throws Exception {
    listFor("?filter.eq.iterationId=920")
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }
}
