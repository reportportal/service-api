package com.epam.reportportal.base.core.tms.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.aifactory.connector.CiHttpClient;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The review of an AI test case from the first comment to the fixed case, request by request, against the real
 * application (stories US-AI-FCTRY-011 and US-AI-FCTRY-012): an AI Draft case that a generation iteration produced,
 * two review comments, "Push to agent", the agent reporting its work as a new iteration that re-runs the first
 * one, the fixed version arriving with its grade and the answers to the comments, Auto-Ready promoting the case,
 * and a second round whose job fails.
 * <p>
 * The CI system is the only thing that is faked: starting the fix workflow is a recorded call. The test commits
 * (the Library announces a change to the pipeline only once it is committed), and the cleanup script removes what
 * it created.
 */
@Sql("/db/tms/fixround/fix-round-fill.sql")
@Sql(scripts = "/db/tms/fixround/fix-round-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TestCaseFixRoundE2eIntegrationTest extends BaseMvcTest {

  private static final String PROJECT = "superadmin_personal";
  private static final String PIPELINES = "/v1/project/" + PROJECT + "/pipeline";
  private static final String TEST_CASES = "/v1/project/" + PROJECT + "/tms/test-case";
  private static final long LIBRARY_FOLDER_ID = 920;
  private static final String[] CRITERIA = {"atomicity", "clear_steps", "clear_expected_results", "no_invented_logic",
      "no_invented_ui", "coherence"};

  private static TimeZone originalTimeZone;

  /**
   * The shared {@code JpaInstantConverter} writes UTC and reads in the JVM zone, so a machine that is not on UTC
   * shifts times every time an entity is saved again. Production runs on UTC; so does this test.
   */
  @BeforeAll
  static void runOnUtc() {
    originalTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @AfterAll
  static void restoreTimeZone() {
    TimeZone.setDefault(originalTimeZone);
  }

  @MockBean
  private CiHttpClient ciHttpClient;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  private long testCaseId;
  private String displayId;
  private long pipelineId;
  private long firstIterationId;
  private long firstVersionId;

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void fullScenario_FromTheFirstCommentToTheFixedCaseAndAFailedRound() throws Exception {
    step1_aGenerationIterationProducedAnAiDraftCase();
    step2_editorCommentsAndCannotApproveWhileCommentsAreUnsent();
    step3_startingTheRoundStartsTheFixJobWithIdsOnly();
    step4_whileTheAgentIsFixingTheCaseIsLocked();
    step5_theJobReadsItsRoundAndReportsItsIteration();
    step6_theResultArrivesAndAutoReadyPromotesTheCase();
    step7_theFixedCaseKeepsItsHistoryAndTheCommentsAreAnswered();
    step8_theIterationsKnowTheRound();
    step9_commentsNeverChangeTheLifecycleAndAFailedRoundGivesTheCommentsBack();
  }

  // ------------------------------------------------------------------ step 1

  private void step1_aGenerationIterationProducedAnAiDraftCase() throws Exception {
    var created = send(post(TEST_CASES), """
        {"name": "Test Case Library. Bulk selection. 'Add to Test Plan' is disabled when nothing is selected",
         "priority": "HIGH", "testFolderId": %d, "status": "DRAFT"}
        """.formatted(LIBRARY_FOLDER_ID), HttpStatus.OK);
    testCaseId = created.get("id").asLong();
    displayId = created.get("displayId").asText();
    reportGrade(testCaseId, new int[] {15, 12, 14, 13, 10, 8}, "0.235");

    var iteration = send(post(PIPELINES + "/iteration").header("Idempotency-Key", "fix-round-gen-1"), """
        {"name": "Test case generation", "type": "TEST_CASE_GENERATION",
         "iteration": {"startedAt": "2026-10-20T10:00:00Z",
           "attributes": [{"key": "requirement", "label": "Requirement", "value": "US-TMS-BLK-001", "type": "META"}],
           "stages": [
             {"stageKey": "create", "name": "Create", "sequence": 1, "status": "Passed", "execution": %s},
             {"stageKey": "upload", "name": "Upload", "sequence": 2, "status": "Passed", "execution": %s,
              "sections": [%s]}
           ]}}
        """.formatted(execution("101"), execution("102"), uploadReport(displayId)), HttpStatus.CREATED);
    firstIterationId = iteration.get("id").asLong();
    pipelineId = iteration.get("pipelineId").asLong();

    var review = stage(iteration, "Review");
    assertEquals("0/1 Ready", review.get("summary").asText());
    var testCase = libraryCase();
    assertEquals("AI", testCase.get("origin").asText());
    assertEquals("DRAFT", testCase.get("status").asText());
    assertEquals(72, testCase.get("metrics").get("overallScore").asInt());
    firstVersionId = testCase.get("defaultVersionId").asLong();

    send(patch(PIPELINES + "/" + pipelineId),
        "{\"settings\": {\"autoReady\": {\"enabled\": true, \"threshold\": 90}}}", HttpStatus.OK);
  }

  // ------------------------------------------------------------------ step 2

  private void step2_editorCommentsAndCannotApproveWhileCommentsAreUnsent() throws Exception {
    var first = send(post(reviewUrl("/comment")),
        "{\"targetType\": \"STEP\", \"stepNumber\": 2, \"text\": \"Name the exact toast text\"}", HttpStatus.CREATED);
    var second = send(post(reviewUrl("/comment")),
        "{\"targetType\": \"PRECONDITION\", \"text\": \"Split this case into two\"}", HttpStatus.CREATED);
    assertEquals("NOT_SENT", first.get("state").asText());
    assertEquals(2, first.get("stepNumber").asInt());
    assertFalse(second.has("stepNumber"));

    var review = send(get(reviewUrl("")), null, HttpStatus.OK);
    assertEquals(2, review.get("comments").size());
    assertEquals(2, review.get("unsentCount").asInt());
    assertFalse(review.get("fixInProgress").asBoolean());

    send(patch(TEST_CASES + "/" + testCaseId), "{\"status\": \"READY\"}", HttpStatus.CONFLICT);
    assertEquals("DRAFT", libraryCase().get("status").asText(), "Approve is blocked while comments are not sent");
  }

  // ------------------------------------------------------------------ step 3

  private void step3_startingTheRoundStartsTheFixJobWithIdsOnly() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    var round = send(post(reviewUrl("/fix-round")), null, HttpStatus.OK);

    assertEquals(1, round.get("roundNumber").asInt());
    assertEquals("PENDING", round.get("status").asText());
    assertEquals(firstIterationId, round.get("previousIterationId").asLong());
    assertEquals(2, round.get("comments").size());
    assertEquals("SENT", round.get("comments").get(0).get("state").asText());
    assertEquals("https://github.com/org/repo/actions/workflows/fix-test-case.yml", round.get("workflowUrl").asText());

    var url = ArgumentCaptor.forClass(String.class);
    var body = ArgumentCaptor.forClass(String.class);
    verify(ciHttpClient, atLeastOnce()).postForStatus(url.capture(), anyMap(), body.capture());
    assertEquals("https://api.github.com/repos/org/repo/actions/workflows/fix-test-case.yml/dispatches", url.getValue());
    var inputs = mapper.readTree(body.getValue()).get("inputs");
    assertEquals(String.valueOf(testCaseId), inputs.get("test_case_id").asText());
    assertEquals(round.get("id").asText(), inputs.get("fix_round_id").asText());
    assertEquals(String.valueOf(firstIterationId), inputs.get("previous_iteration_id").asText());
    assertEquals(3, inputs.size(), "the comments themselves never travel through the workflow inputs");
  }

  // ------------------------------------------------------------------ step 4

  private void step4_whileTheAgentIsFixingTheCaseIsLocked() throws Exception {
    var review = send(get(reviewUrl("")), null, HttpStatus.OK);
    assertTrue(review.get("fixInProgress").asBoolean());
    assertEquals(1, review.get("fixRound").asInt());
    assertEquals(0, review.get("unsentCount").asInt());

    send(post(reviewUrl("/comment")), "{\"targetType\": \"PRECONDITION\", \"text\": \"one more\"}", HttpStatus.CONFLICT);
    send(delete(reviewUrl("/comment")), null, HttpStatus.CONFLICT);
    send(post(reviewUrl("/fix-round")), null, HttpStatus.CONFLICT);
    send(patch(TEST_CASES + "/" + testCaseId), "{\"status\": \"READY\"}", HttpStatus.CONFLICT);
  }

  // ------------------------------------------------------------------ step 5

  private long roundId;
  private long secondIterationId;
  private JsonNode roundComments;

  private void step5_theJobReadsItsRoundAndReportsItsIteration() throws Exception {
    roundId = send(get(reviewUrl("")), null, HttpStatus.OK).get("comments").get(0).get("fixRoundId").asLong();
    var round = send(get(reviewUrl("/fix-round/" + roundId)), null, HttpStatus.OK);
    roundComments = round.get("comments");
    assertEquals(2, roundComments.size());
    assertEquals("Name the exact toast text", roundComments.get(0).get("text").asText());

    var iteration = send(post(PIPELINES + "/iteration").header("Idempotency-Key", "fix-round-fix-1"), """
        {"name": "Test case generation", "type": "TEST_CASE_GENERATION",
         "iteration": {"startedAt": "2026-10-20T11:00:00Z", "rerunOfIterationId": %d,
           "attributes": [{"key": "requirement", "label": "Requirement", "value": "US-TMS-BLK-001", "type": "META"},
                          {"key": "fixRoundId", "label": "Fix round", "value": "%d", "type": "NONE"}],
           "stages": [{"stageKey": "fix", "name": "Fix", "sequence": 1, "status": "Running", "execution": %s}]}}
        """.formatted(firstIterationId, roundId, execution("201")), HttpStatus.CREATED);
    secondIterationId = iteration.get("id").asLong();
    assertEquals(firstIterationId, iteration.get("rerunOf").get("id").asLong());
  }

  // ------------------------------------------------------------------ step 6

  private void step6_theResultArrivesAndAutoReadyPromotesTheCase() throws Exception {
    var fixed = send(put(reviewUrl("/fix-round/" + roundId + "/result")), """
        {"pipelineIterationId": %d, "name": "Test Case Library. Bulk selection. 'Add to Test Plan' is disabled with no selection",
         "manualScenario": {"manualScenarioType": "STEPS",
           "steps": [{"instructions": "Open the Library and select no Test Case", "expectedResult": "The bulk panel is hidden"},
                     {"instructions": "Look at the toast", "expectedResult": "The toast says 'Select at least one Test Case'"}]},
         "qualityScores": %s,
         "generation": {"model": "auto (default)", "costUsd": 0.22},
         "commentResults": [{"commentId": %d, "addressed": true},
                            {"commentId": %d, "addressed": false, "reason": "Needs a new case; a fix round cannot create cases"}]}
        """.formatted(secondIterationId, scores(new int[] {15, 20, 18, 18, 14, 9}),
        roundComments.get(0).get("id").asLong(), roundComments.get(1).get("id").asLong()), HttpStatus.OK);

    assertEquals(94, fixed.get("metrics").get("overallScore").asInt());
    assertEquals("READY", fixed.get("status").asText(), "94 >= 90: promoted by Auto-Ready after the update");
    assertNotEquals(firstVersionId, fixed.get("defaultVersionId").asLong(), "the fix is a new default version");
    assertEquals(2, fixed.get("manualScenario").get("steps").size());
    send(put(reviewUrl("/fix-round/" + roundId + "/result")),
        "{\"manualScenario\": {\"manualScenarioType\": \"STEPS\", \"steps\": []}}", HttpStatus.CONFLICT);
  }

  // ------------------------------------------------------------------ step 7

  private void step7_theFixedCaseKeepsItsHistoryAndTheCommentsAreAnswered() throws Exception {
    assertEquals(2, count("select count(*) from tms_test_case_version where test_case_id = ?", testCaseId));
    assertEquals(1, count("select count(*) from tms_test_case_version where test_case_id = ? and is_default", testCaseId));
    assertEquals(6, count("select count(*) from tms_test_case_quality_score where test_case_version_id = ?", firstVersionId),
        "the grade of the first version stays: that is the score before the fix");
    assertEquals(secondIterationId, jdbcTemplate.queryForObject(
        "select pipeline_iteration_id from tms_test_case_version where test_case_id = ? and is_default", Long.class,
        testCaseId));
    assertEquals(secondIterationId, jdbcTemplate.queryForObject(
        "select max(s.pipeline_iteration_id) from tms_test_case_quality_score s join tms_test_case_version v "
            + "on v.id = s.test_case_version_id where v.test_case_id = ?", Long.class, testCaseId));
    assertEquals(true, jdbcTemplate.queryForObject("select modified_by_agent from tms_test_case where id = ?",
        Boolean.class, testCaseId));

    var review = send(get(reviewUrl("")), null, HttpStatus.OK);
    assertFalse(review.get("fixInProgress").asBoolean());
    assertEquals(0, review.get("unsentCount").asInt());
    var comments = review.get("comments");
    assertEquals("ADDRESSED", comments.get(0).get("state").asText());
    assertEquals(1, comments.get(0).get("roundNumber").asInt());
    assertEquals("NOT_ADDRESSED", comments.get(1).get("state").asText());
    assertEquals("Needs a new case; a fix round cannot create cases", comments.get(1).get("agentReason").asText());
  }

  // ------------------------------------------------------------------ step 8

  private void step8_theIterationsKnowTheRound() throws Exception {
    var second = send(get(PIPELINES + "/iteration/" + secondIterationId), null, HttpStatus.OK);
    assertEquals(displayId + " · round 1", attribute(second, "fixRound"), "a fix iteration is told from a plain re-run");
    assertEquals(firstIterationId, second.get("rerunOf").get("id").asLong());

    var uploaded = send(put(PIPELINES + "/iteration/" + secondIterationId + "/stage/upload"), """
        {"name": "Upload", "sequence": 2, "status": "Passed", "execution": %s, "sections": [%s]}
        """.formatted(execution("202"), uploadReport(displayId)), HttpStatus.OK);
    assertEquals("1/1 Ready", stage(uploaded, "Review").get("summary").asText());

    var first = send(get(PIPELINES + "/iteration/" + firstIterationId), null, HttpStatus.OK);
    assertEquals("1/1 Ready", stage(first, "Review").get("summary").asText(),
        "the first iteration recounts its review: its case went Ready");
  }

  // ------------------------------------------------------------------ step 9

  private void step9_commentsNeverChangeTheLifecycleAndAFailedRoundGivesTheCommentsBack() throws Exception {
    send(post(reviewUrl("/comment")), "{\"targetType\": \"STEP\", \"stepNumber\": 1, \"text\": \"Too vague\"}",
        HttpStatus.CREATED);
    assertEquals("READY", libraryCase().get("status").asText(), "adding a comment does not change the lifecycle");
    reset(ciHttpClient);
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");

    var round = send(post(reviewUrl("/fix-round")), null, HttpStatus.OK);
    assertEquals(2, round.get("roundNumber").asInt());
    assertEquals(secondIterationId, round.get("previousIterationId").asLong(),
        "round 2 follows the iteration that last fixed the case");
    var secondRoundId = round.get("id").asLong();

    var third = send(post(PIPELINES + "/iteration").header("Idempotency-Key", "fix-round-fix-2"), """
        {"name": "Test case generation", "type": "TEST_CASE_GENERATION",
         "iteration": {"startedAt": "2026-10-20T12:00:00Z", "rerunOfIterationId": %d,
           "attributes": [{"key": "fixRoundId", "value": "%d", "type": "NONE"}],
           "stages": [{"stageKey": "fix", "name": "Fix", "sequence": 1, "status": "Failed",
                       "message": "The agent could not apply the comment"}]}}
        """.formatted(secondIterationId, secondRoundId), HttpStatus.CREATED);
    assertEquals("FAILED", third.get("statusCategory").asText());

    var review = send(get(reviewUrl("")), null, HttpStatus.OK);
    assertFalse(review.get("fixInProgress").asBoolean());
    assertEquals(1, review.get("unsentCount").asInt(), "the comment of the failed round is not sent again");
    assertEquals("READY", libraryCase().get("status").asText(), "a failed round leaves the case as it was");
    assertEquals("FAILED", jdbcTemplate.queryForObject("select status from tms_test_case_fix_round where id = ?",
        String.class, secondRoundId));
    assertEquals(2, count("select count(*) from tms_test_case_version where test_case_id = ?", testCaseId),
        "a failed round creates no version");

    send(post(reviewUrl("/fix-round")), null, HttpStatus.OK);
    send(delete(reviewUrl("/comment")), null, HttpStatus.CONFLICT);
  }

  // ------------------------------------------------------------------ building requests

  private String reviewUrl(String path) {
    return TEST_CASES + "/" + testCaseId + "/review" + path;
  }

  private void reportGrade(long id, int[] scores, String cost) throws Exception {
    send(post(TEST_CASES + "/" + id + "/generation"), "{\"qualityScores\": " + scores(scores)
        + ", \"generation\": {\"model\": \"auto (default)\", \"costUsd\": " + cost + "}}", HttpStatus.OK);
  }

  private String scores(int[] scores) {
    var array = mapper.createArrayNode();
    for (int i = 0; i < CRITERIA.length; i++) {
      array.addObject().put("criterionKey", CRITERIA[i]).put("score", scores[i]);
    }
    return array.toString();
  }

  private static String execution(String jobId) {
    return """
        {"executorType": "GITHUB_ACTIONS", "externalRunId": "%s", "retryable": true,
         "runUrl": "https://github.com/org/repo/actions/runs/%s",
         "retryParameters": {"repo": "org/repo", "workflowRef": "ai-pipeline.yml"}}
        """.formatted(jobId, jobId);
  }

  private static String uploadReport(String testCaseDisplayId) {
    return """
        {"key": "uploadReport", "title": "Upload report", "role": "RESULT",
         "columns": [{"key": "testCase", "label": "Test case"}, {"key": "result", "label": "Result"}],
         "rows": [{"key": "case-1", "cells": {
           "testCase": {"value": "%s", "target": {"entityType": "TEST_CASE", "entityId": "%s"}},
           "result": "Uploaded"}}]}
        """.formatted(testCaseDisplayId, testCaseDisplayId);
  }

  // ------------------------------------------------------------------ calls and reading

  private JsonNode libraryCase() throws Exception {
    return send(get(TEST_CASES + "/" + testCaseId), null, HttpStatus.OK);
  }

  private long count(String sql, Object... args) {
    return jdbcTemplate.queryForObject(sql, Long.class, args);
  }

  private JsonNode send(MockHttpServletRequestBuilder request, String body, HttpStatus expected) throws Exception {
    request.with(token(oAuthHelper.getSuperadminToken()));
    if (body != null) {
      request.contentType(APPLICATION_JSON).content(body);
    }
    var response = mockMvc.perform(request).andExpect(status().is(expected.value())).andReturn().getResponse();
    var content = response.getContentAsString();
    return content.isBlank() ? mapper.createObjectNode() : mapper.readTree(content);
  }

  private static JsonNode stage(JsonNode iteration, String name) {
    for (var stage : iteration.path("stages")) {
      if (name.equals(stage.path("name").asText())) {
        return stage;
      }
    }
    throw new AssertionError("no stage '" + name + "' in " + iteration.path("stages"));
  }

  private static String attribute(JsonNode owner, String key) {
    for (var attribute : owner.path("attributes")) {
      if (key.equals(attribute.path("key").asText())) {
        return attribute.path("value").asText();
      }
    }
    return null;
  }
}
