package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.aifactory.connector.CiHttpClient;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Automate: {@code POST /v1/project/superadmin_personal/tms/automation} sends Ready cases to the Test automation
 * pipeline and {@code GET .../tms/test-case/{id}} then shows their {@code automationState}.
 * <p>
 * Fixture: cases 900 and 901 (READY) and a complete GitLab CI integration in project 1 (no pipeline of its own for
 * test automation yet, so the project integration decides whether jobs may start). {@link CiHttpClient} is the only
 * mock, so the controller, services, repositories and the GitLab connector run for real.
 * <p>
 * The start does its database work in independent transactions, so, like the retry tests, these tests run without
 * the rolled-back test transaction and the cleanup scripts remove what they commit.
 */
@Sql("/db/aifactory/pipeline/pipeline-fill.sql")
@Sql("/db/aifactory/pipeline/automation-fill.sql")
@Sql(scripts = "/db/aifactory/pipeline/automation-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/db/aifactory/pipeline/pipeline-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TmsAutomationIntegrationTest extends BaseMvcTest {

  private static final String AUTOMATION = "/v1/project/superadmin_personal/tms/automation";
  private static final String TEST_CASES = "/v1/project/superadmin_personal/tms/test-case";

  @MockitoBean
  private CiHttpClient ciHttpClient;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final ObjectMapper mapper = new ObjectMapper();

  private ResultActions automate(List<Long> ids, String environment, boolean confirm, List<Long> confirmed)
      throws Exception {
    return mockMvc.perform(post(AUTOMATION)
        .with(token(oAuthHelper.getSuperadminToken()))
        .contentType(APPLICATION_JSON)
        .content(mapper.writeValueAsString(Map.of("testCaseIds", ids, "environment", environment,
            "confirmReautomate", confirm, "confirmedTestCaseIds", confirmed))));
  }

  private ResultActions automate(List<Long> ids) throws Exception {
    return automate(ids, "beta5", false, List.of());
  }

  private ResultActions testCase(long id) throws Exception {
    return mockMvc.perform(get(TEST_CASES + "/" + id).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
  }

  private long count(String sql) {
    return jdbcTemplate.queryForObject(sql, Long.class);
  }

  @Test
  void start_ShouldCreateOneIterationForAllAcceptedCasesAndStartTheJobWithTheirIds() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    automate(List.of(900L, 901L))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.accepted.length()").value(2))
        .andExpect(jsonPath("$.skipped.length()").value(0))
        .andExpect(jsonPath("$.iteration.number").value(1));

    var url = ArgumentCaptor.forClass(String.class);
    var body = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient).postForStatus(url.capture(), anyMap(), body.capture());
    assertEquals("https://git.epam.com/api/v4/projects/EPM-RPP%2Frp-tests/trigger/pipeline", url.getValue());
    assertTrue(body.getValue().contains("\"TEST_CASES\":\"900,901\""), body.getValue());
    assertTrue(body.getValue().contains("\"ENVIRONMENT\":\"beta5\""), body.getValue());
    assertTrue(body.getValue().contains("\"ITERATION_ID\":\""), body.getValue());
    assertTrue(body.getValue().contains("\"PIPELINE_ID\":\""), body.getValue());
    assertEquals(1, count("select count(*) from pipeline_iteration where pipeline_id in "
        + "(select id from pipeline where type = 'TEST_AUTOMATION' and project_id = 1)"));
    assertEquals(2, count("select count(*) from tms_test_case_automation_run where project_id = 1"));
  }

  @Test
  void testCase_AfterAutomate_ShouldShowItInProgressWithItsIterationAndTheCiState() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");
    automate(List.of(900L)).andExpect(status().isAccepted());

    testCase(900)
        .andExpect(jsonPath("$.automationState.status").value("IN_PROGRESS"))
        .andExpect(jsonPath("$.automationState.ciConnection").value("CONNECTED"))
        .andExpect(jsonPath("$.automationState.fixInProgress").value(false))
        .andExpect(jsonPath("$.automationState.iteration.number").value(1))
        .andExpect(jsonPath("$.automationState.iteration.iterationId").isNumber());
    testCase(901)
        .andExpect(jsonPath("$.automationState.status").value("NOT_AUTOMATED"))
        .andExpect(jsonPath("$.automationState.iteration").doesNotExist());
  }

  @Test
  void start_ForACaseAlreadyInAnAutomationIteration_ShouldSkipItAndAnswer409WhenNothingIsLeft() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");
    automate(List.of(900L)).andExpect(status().isAccepted());

    automate(List.of(900L))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.reason").value("NO_ELIGIBLE_TEST_CASES"))
        .andExpect(jsonPath("$.skipped[0].id").value(900))
        .andExpect(jsonPath("$.skipped[0].reason").value("AUTOMATION_IN_PROGRESS"));
    assertEquals(1, count("select count(*) from pipeline_iteration where pipeline_id in "
        + "(select id from pipeline where type = 'TEST_AUTOMATION' and project_id = 1)"));
  }

  @Test
  void start_WithADraftCase_ShouldAcceptTheOthersAndSkipTheDraftOne() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");
    jdbcTemplate.update("update tms_test_case set status = 'DRAFT' where id = 901");

    automate(List.of(900L, 901L))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.accepted[0]").value(900))
        .andExpect(jsonPath("$.accepted.length()").value(1))
        .andExpect(jsonPath("$.skipped[0].id").value(901))
        .andExpect(jsonPath("$.skipped[0].reason").value("NOT_READY"));
  }

  @Test
  void start_ForACaseTheAgentIsFixing_ShouldSkipIt() throws Exception {
    jdbcTemplate.update("insert into tms_test_case_fix_round (test_case_id, project_id, round_number, status, "
        + "previous_iteration_id, requested_by, requested_at) values (900, 1, 1, 'PENDING', 900, 1, now())");
    try {
      automate(List.of(900L))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.skipped[0].reason").value("FIX_RUNNING"));
      testCase(900).andExpect(jsonPath("$.automationState.fixInProgress").value(true));
    } finally {
      jdbcTemplate.update("delete from tms_test_case_fix_round where test_case_id = 900");
    }
  }

  @Test
  void start_WhenTheCiRefusesIt_ShouldLeaveNoIterationAndNoRunBehind() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString()))
        .thenThrow(new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "HTTP code: 500"));

    automate(List.of(900L)).andExpect(status().isBadRequest());

    assertEquals(0, count("select count(*) from tms_test_case_automation_run where project_id = 1"));
    assertEquals(0, count("select count(*) from pipeline_iteration where pipeline_id in "
        + "(select id from pipeline where type = 'TEST_AUTOMATION' and project_id = 1)"));
    testCase(900).andExpect(jsonPath("$.automationState.status").value("NOT_AUTOMATED"));
  }

  @Test
  void start_ForAnAutomatedCase_ShouldAskForConfirmationFirstAndStartNothing() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");
    automate(List.of(900L)).andExpect(status().isAccepted());
    jdbcTemplate.update("update pipeline_iteration set status = 'Passed', status_category = 'PASSED' where "
        + "pipeline_id in (select id from pipeline where type = 'TEST_AUTOMATION' and project_id = 1)");
    testCase(900).andExpect(jsonPath("$.automationState.status").value("AUTOMATED"));

    automate(List.of(900L))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.reason").value("ALREADY_AUTOMATED_CONFIRM_REQUIRED"))
        .andExpect(jsonPath("$.testCaseIds[0]").value(900));
    verify(ciHttpClient, org.mockito.Mockito.times(1)).postForStatus(anyString(), anyMap(), anyString());

    automate(List.of(900L), "beta5", true, List.of(900L))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.iteration.number").value(2));
  }

  @Test
  void start_WithAnIdThatIsNotACaseOfTheProject_ShouldAnswer404() throws Exception {
    automate(List.of(900L, 99999L)).andExpect(status().isNotFound());

    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
  }

  @Test
  void start_WhileThePipelineCannotStartJobs_ShouldAnswer409WithoutStartingAnything() throws Exception {
    jdbcTemplate.update("update integration set enabled = false where id in (900, 910)");

    automate(List.of(900L))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value(ErrorType.CI_NOT_CONNECTED.getCode()));
    verify(ciHttpClient, never()).postForStatus(anyString(), anyMap(), anyString());
    assertEquals(0, count("select count(*) from tms_test_case_automation_run where project_id = 1"));
  }

  @Test
  void environments_WhenNoneIsConfigured_ShouldAnswer400() throws Exception {
    mockMvc.perform(get(AUTOMATION + "/environment").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }
}
