package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The end-to-end scenario of {@code doc/pipeline-iteration-e2e-journey.md}, request by request, against the real
 * application: pipeline "Test case generation", iteration #1 with the stages Create, Grade, Upload and the
 * Review stage kept by ReportPortal, four test cases of which Auto-Ready promotes two, and an editor who
 * marks the other two Ready.
 * <p>
 * Every step is one private method named after the step of the document, so a failure points at the
 * document. The test cases are created through the Test Case Library API like the agent does, so the Library
 * ids in the reports are the real ones. The test commits (the Library announces a change to the pipeline only
 * once it is committed), and the cleanup script removes what it created.
 */
@Sql("/db/aifactory/e2e/pipeline-e2e-fill.sql")
@Sql(scripts = "/db/aifactory/e2e/pipeline-e2e-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PipelineIterationE2eIntegrationTest extends BaseMvcTest {

  private static final String PROJECT = "superadmin_personal";
  private static final String PIPELINES = "/v1/project/" + PROJECT + "/pipeline";
  private static final String TEST_CASES = "/v1/project/" + PROJECT + "/tms/test-case";
  private static final long LIBRARY_FOLDER_ID = 910;
  private static final String RUN_URL = "https://gitlab.epam.com/epm-rpp/rp-tests/-/jobs/";

  /** A case of the scenario: what the agent authored, how the grader scored it, and what it criticised. */
  private record AuthoredCase(String name, String priority, int[] scores, String[] reasons) {

    int total() {
      int sum = 0;
      for (int score : scores) {
        sum += score;
      }
      return sum;
    }
  }

  private static final String[] CRITERIA = {"atomicity", "clear_steps", "clear_expected_results", "no_invented_logic",
      "no_invented_ui", "coherence"};
  private static final String[] CRITERION_LABELS = {"Atomicity", "Clear steps", "Clear expected results",
      "No invented logic", "No invented UI", "Coherence"};
  private static final int[] CRITERION_MAX = {15, 20, 20, 20, 15, 10};

  private static final List<AuthoredCase> CASES = List.of(
      new AuthoredCase("Test Case Library. Empty state. Empty state is displayed for a project without Test Cases",
          "HIGH", new int[] {15, 20, 18, 18, 15, 8},
          new String[] {null, null, "Step 3 does not name the message that must be visible",
              "Step 2 assumes a default project exists", null, "Title is broader than the steps"}),
      new AuthoredCase("Test Case Library. Empty state. 'Create Test Case' button opens the creation modal",
          "MEDIUM", new int[] {15, 20, 17, 20, 11, 8},
          new String[] {null, null, "Modal title is not stated", null, "Button label is not in the requirement",
              "Title is broader than the steps"}),
      new AuthoredCase("Test Case Library. Navigation panel. Folder tree is hidden while the Library is empty",
          "LOW", new int[] {15, 12, 14, 13, 10, 8},
          new String[] {null, "Two actions in one step", "Expected result is not observable",
              "Assumes folders can exist in an empty Library", "Panel name is not in the requirement",
              "Title is broader than the steps"}),
      new AuthoredCase("Test Case Library. Empty state. 'Import' button opens the import modal",
          "LOW", new int[] {8, 12, 10, 10, 10, 8},
          new String[] {"Covers button and modal content", "Step 1 is vague", "No expected result for the modal",
              "Invents a file type check", "Invents a drop zone", "Title is broader than the steps"}));

  private static TimeZone originalTimeZone;

  /**
   * The shared {@code JpaInstantConverter} writes UTC and reads in the JVM zone, so a machine that is not on UTC
   * shifts an iteration's times every time it is saved again. Production runs on UTC; so does this test.
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

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  /** Library display ids of the four cases, known once step 10 created them (the Library makes them). */
  private final List<String> libraryIds = new ArrayList<>();
  private final List<Long> libraryEntityIds = new ArrayList<>();

  private long pipelineId;
  private long iterationId;

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void fullScenario_FromTheFirstReportToAllTestCasesReady() throws Exception {
    step1_ciStartsAndCreatesThePipelineAndIteration();
    step2_pipelinesPageShowsTheRunningIteration();
    step3_iterationCardsOfThePipeline();
    step4_iterationDetailsWhileCreateRuns();
    step5_editorSwitchesAutoReadyOn();
    step6_createFinishes();
    step7_gradeStarts();
    step8_gradeFinishesWithTheRawOutputAndTheTable();
    step9_suiteScoreGoesToTheIteration();
    step10_agentWritesTheTestCasesToTheLibrary();
    step11_uploadStarts();
    step12_uploadFinishesAndAutoReadyAndReviewComeAlive();
    step13_libraryIdsGoBackIntoCreateAndGradeTables();
    step14_ciPipelineEnds();
    step15_whatTheUiReadsNow();
    editorMarksTheOtherTwoCasesReady_IterationCompletes();
  }

  // ------------------------------------------------------------------ step 1

  private void step1_ciStartsAndCreatesThePipelineAndIteration() throws Exception {
    var rq = """
        {
          "name": "Test case generation",
          "type": "TEST_CASE_GENERATION",
          "attributes": [
            {"key": "repository", "label": "Repository", "value": "EPM-RPP/rp-tests", "type": "META",
             "valueType": "LINK", "url": "https://gitlab.epam.com/epm-rpp/rp-tests"}
          ],
          "iteration": {
            "startedAt": "2026-09-19T10:13:00Z",
            "attributes": [
              {"key": "requirement", "label": "Requirement", "value": "US-TMS-MIG-001",
               "description": "Test Case Library empty state display and navigation panel",
               "type": "META", "order": 1, "comparisonKey": true},
              {"key": "trigger", "label": "Trigger", "value": "Web form · REQUIREMENT", "type": "META", "order": 2},
              {"key": "model", "label": "Model", "value": "auto (default)", "type": "META", "order": 3},
              {"key": "env", "label": "Environment", "value": "beta5", "type": "TAG"},
              {"key": "spec", "label": "Spec", "value": "US-TMS-MIG-001", "type": "TAG"},
              {"key": "jira", "label": "Jira", "value": "EPMRPP-104212", "type": "TAG", "valueType": "LINK",
               "url": "https://jira.epam.com/browse/EPMRPP-104212"},
              {"key": "ci", "label": "CI pipeline", "value": "#1284551", "type": "TAG", "valueType": "LINK",
               "url": "https://gitlab.epam.com/epm-rpp/rp-tests/-/pipelines/1284551"},
              {"key": "folder", "label": "Library folder", "value": "tms/library/emptystate/iteration-1", "type": "TAG"}
            ],
            "stages": [
              {"stageKey": "create", "name": "Create", "sequence": 1, "status": "Running",
               "startedAt": "2026-09-19T10:13:00Z", "message": "Authoring test cases from US-TMS-MIG-001",
               "attributes": [{"key": "model", "label": "Model", "value": "auto (default)", "type": "NONE"}],
               "execution": %s},
              {"stageKey": "grade", "name": "Grade", "sequence": 2, "status": "Pending",
               "affectsIterationStatus": false, "message": "Waiting for Create"},
              {"stageKey": "upload", "name": "Upload", "sequence": 3, "status": "Pending",
               "message": "Waiting for Grade"}
            ]
          }
        }
        """.formatted(execution("8812301"));

    var detail = send(post(PIPELINES + "/iteration").header("Idempotency-Key", "gitlab-pipeline-1284551"), rq,
        HttpStatus.CREATED);

    iterationId = detail.get("id").asLong();
    pipelineId = detail.get("pipelineId").asLong();
    assertEquals("Test case generation", detail.get("pipelineName").asText());
    assertEquals("TEST_CASE_GENERATION", detail.get("pipelineType").asText());
    assertEquals(1, detail.get("iterationNumber").asInt());
    assertIterationStatus(detail, "Running", "RUNNING");
    assertEquals("2026-09-19T10:13:00Z", detail.get("startedAt").asText(), "step1 startedAt");
    assertEquals(8, detail.get("attributes").size());
    assertEquals("US-TMS-MIG-001", attribute(detail, "requirement"));
    assertTrue(detail.get("attributes").get(0).get("comparisonKey").asBoolean());
    assertNotNull(detail.get("startedBy"));
    assertEquals(4, detail.get("stages").size());

    var create = stage(detail, "Create");
    assertStage(create, "Running", "RUNNING", "PIPELINE", true);
    assertEquals(RUN_URL + "8812301", create.get("links").get(0).get("url").asText());
    assertEquals("RUN", create.get("links").get(0).get("type").asText());
    assertTrue(create.get("retryable").asBoolean());
    assertStage(stage(detail, "Grade"), "Pending", "PENDING", "PIPELINE", false);
    assertStage(stage(detail, "Upload"), "Pending", "PENDING", "PIPELINE", true);
    var review = stage(detail, "Review");
    assertStage(review, "Pending", "PENDING", "REPORTPORTAL", false);
    assertEquals("Waiting for Upload", review.get("message").asText());
  }

  // ------------------------------------------------------------------ steps 2-4

  private void step2_pipelinesPageShowsTheRunningIteration() throws Exception {
    var page = send(get(PIPELINES).param("offset", "0").param("limit", "100"), null, HttpStatus.OK);

    var pipeline = page.get("content").get(0);
    assertEquals(1, page.get("content").size());
    assertEquals("Test case generation", pipeline.get("name").asText());
    assertEquals("TEST_CASE_GENERATION", pipeline.get("type").asText());
    assertEquals("EPM-RPP/rp-tests", attribute(pipeline, "repository"));
    assertEquals(1, pipeline.get("iterationsCount").asInt());
    assertFalse(pipeline.has("settings"), "Auto-Ready is not switched on yet");
    assertFalse(pipeline.has("latestIteration"), "iterations are read by expanding the group (step 3)");
  }

  private void step3_iterationCardsOfThePipeline() throws Exception {
    var cards = send(get(PIPELINES + "/" + pipelineId + "/iteration").param("offset", "0").param("limit", "100"),
        null, HttpStatus.OK);
    assertEquals(1, cards.get("content").size());
    assertEquals(iterationId, cards.get("content").get(0).get("id").asLong());

    assertEquals(1, send(get(PIPELINES + "/" + pipelineId + "/iteration").param("search", "US-TMS-MIG"), null,
        HttpStatus.OK).get("content").size());
    assertEquals(1, send(get(PIPELINES + "/" + pipelineId + "/iteration").param("status", "RUNNING"), null,
        HttpStatus.OK).get("content").size());
    assertEquals(1, send(get(PIPELINES + "/" + pipelineId + "/iteration")
        .param("attribute", "requirement:US-TMS-MIG-001"), null, HttpStatus.OK).get("content").size());
  }

  private void step4_iterationDetailsWhileCreateRuns() throws Exception {
    var detail = iteration();
    assertIterationStatus(detail, "Running", "RUNNING");
    assertEquals("2026-09-19T10:13:00Z", detail.get("startedAt").asText(), "step4 startedAt");
    assertStage(stage(detail, "Create"), "Running", "RUNNING", "PIPELINE", true);
    assertEquals("Authoring test cases from US-TMS-MIG-001", stage(detail, "Create").get("message").asText());
  }

  // ------------------------------------------------------------------ step 5

  private void step5_editorSwitchesAutoReadyOn() throws Exception {
    var pipeline = send(patch(PIPELINES + "/" + pipelineId),
        "{\"settings\": {\"autoReady\": {\"enabled\": true, \"threshold\": 90}}}", HttpStatus.OK);

    assertTrue(pipeline.get("settings").get("autoReady").get("enabled").asBoolean());
    assertEquals(90, pipeline.get("settings").get("autoReady").get("threshold").asInt());
    send(patch(PIPELINES + "/" + pipelineId), "{\"settings\": {\"autoReady\": {\"enabled\": true}}}",
        HttpStatus.BAD_REQUEST);
  }

  // ------------------------------------------------------------------ step 6

  private void step6_createFinishes() throws Exception {
    var detail = reportStage("create", createStageBody(false));

    assertIterationStatus(detail, "Running", "RUNNING");
    var create = stage(detail, "Create");
    assertStage(create, "Passed", "PASSED", "PIPELINE", true);
    assertEquals(362000, create.get("durationMillis").asLong());
    assertEquals("4 cases", create.get("summary").asText());
    assertEquals("4 Test Cases authored", create.get("message").asText());
    assertEquals(2, create.get("sections").size());
    assertEquals("RESULT", create.get("sections").get(0).get("role").asText());
    assertEquals("DETAILS", create.get("sections").get(1).get("role").asText());
    assertEquals("Passed", cell(create, 0, 0, "status"));
    assertNull(cellNode(create, 0, 0, "libraryId"), "the cases are not in the Library yet");
    assertEquals("4", attribute(detail, "testCases"), "summed from the stages");
    assertMoney("0.94", attribute(detail, "cost"));
    assertStage(stage(detail, "Grade"), "Pending", "PENDING", "PIPELINE", false);
  }

  // ------------------------------------------------------------------ steps 7-9

  private void step7_gradeStarts() throws Exception {
    var detail = reportStage("grade", """
        {"name": "Grade", "status": "Running", "affectsIterationStatus": false,
         "startedAt": "2026-09-19T10:19:10Z", "message": "Grading 4 Test Cases", "execution": %s}
        """.formatted(execution("8812317")));

    var grade = stage(detail, "Grade");
    assertStage(grade, "Running", "RUNNING", "PIPELINE", false);
    assertEquals("Grading 4 Test Cases", grade.get("message").asText());
    assertEquals(RUN_URL + "8812317", grade.get("links").get(0).get("url").asText());
    assertTrue(grade.get("retryable").asBoolean());
    assertIterationStatus(detail, "Running", "RUNNING");
  }

  private void step8_gradeFinishesWithTheRawOutputAndTheTable() throws Exception {
    var detail = reportStage("grade", gradeStageBody(false));

    var grade = stage(detail, "Grade");
    assertStage(grade, "Passed", "PASSED", "PIPELINE", false);
    assertEquals(151000, grade.get("durationMillis").asLong());
    assertEquals("79", grade.get("summary").asText());
    assertEquals("79", attribute(grade, "suiteScore"));
    assertEquals("grade", grade.get("rawOutputSchemaName").asText());
    assertEquals(79, grade.get("rawOutput").get("total_score").asInt());
    var firstRow = grade.get("sections").get(0).get("rows").get(0);
    assertEquals("94", firstRow.get("cells").get("total").get("value").asText());
    assertEquals(3, firstRow.get("details").size(), "one block per criterion that lost points");
    assertEquals("Clear expected results 18/20", firstRow.get("details").get(0).get("heading").asText());
    assertMoney("1.25", attribute(detail, "cost"));
    assertIterationStatus(detail, "Running", "RUNNING");
  }

  private void step9_suiteScoreGoesToTheIteration() throws Exception {
    var detail = send(patch(PIPELINES + "/iteration/" + iterationId), """
        {"attributes": [{"key": "suiteScore", "label": "Suite score", "value": "79", "type": "META",
                         "valueType": "SCORE", "order": 5, "direction": "HIGHER_BETTER"}]}
        """, HttpStatus.OK);

    assertEquals("79", attribute(detail, "suiteScore"));
    assertEquals("SCORE", Objects.requireNonNull(attributeNode(detail, "suiteScore")).get("valueType").asText());
    assertEquals("HIGHER_BETTER", Objects.requireNonNull(attributeNode(detail, "suiteScore")).get("direction").asText());
    assertEquals("folder", detail.get("attributes").get(7).get("key").asText(), "the reported attributes keep their place");
    assertIterationStatus(detail, "Running", "RUNNING");
  }

  // ------------------------------------------------------------------ step 10

  private void step10_agentWritesTheTestCasesToTheLibrary() throws Exception {
    for (var authored : CASES) {
      var created = send(post(TEST_CASES), """
          {"name": "%s", "priority": "%s", "testFolderId": %d, "status": "DRAFT"}
          """.formatted(authored.name(), authored.priority(), LIBRARY_FOLDER_ID), HttpStatus.OK);
      var id = created.get("id").asLong();
      libraryEntityIds.add(id);
      libraryIds.add(created.get("displayId").asText());
      assertEquals("DRAFT", created.get("status").asText());

      var scores = mapper.createArrayNode();
      for (int i = 0; i < CRITERIA.length; i++) {
        scores.addObject().put("criterionKey", CRITERIA[i]).put("score", authored.scores()[i]);
      }
      var generation = mapper.createObjectNode();
      generation.set("qualityScores", scores);
      generation.putObject("generation").put("model", "auto (default)").put("costUsd", 0.235);
      var scored = send(post(TEST_CASES + "/" + id + "/generation"), generation.toString(), HttpStatus.OK);
      assertEquals("AI", scored.get("origin").asText(), "posting scores is what makes the case an AI case");
      assertEquals("DRAFT", scored.get("status").asText());
      assertEquals(authored.total(), scored.get("metrics").get("overallScore").asInt());
    }
    assertEquals(4, libraryIds.stream().distinct().count());
  }

  // ------------------------------------------------------------------ steps 11-12

  private void step11_uploadStarts() throws Exception {
    var detail = reportStage("upload", """
        {"name": "Upload", "status": "Running", "startedAt": "2026-09-19T10:21:50Z",
         "message": "Uploading 4 Test Cases to the Library", "execution": %s}
        """.formatted(execution("8812333")));

    assertStage(stage(detail, "Upload"), "Running", "RUNNING", "PIPELINE", true);
    assertEquals(RUN_URL + "8812333", stage(detail, "Upload").get("links").get(0).get("url").asText());
    assertIterationStatus(detail, "Running", "RUNNING");
  }

  private void step12_uploadFinishesAndAutoReadyAndReviewComeAlive() throws Exception {
    var detail = reportStage("upload", uploadStageBody());

    var upload = stage(detail, "Upload");
    assertStage(upload, "Passed", "PASSED", "PIPELINE", true);
    assertEquals("4 ↑", upload.get("summary").asText());
    assertEquals(14000, upload.get("durationMillis").asLong());
    assertEquals(4, upload.get("relatedEntities").size(), "collected from the cell targets by ReportPortal");
    assertEquals("TEST_CASE", upload.get("relatedEntities").get(0).get("entityType").asText());
    assertEquals(libraryIds.getFirst(), upload.get("relatedEntities").get(0).get("entityId").asText());
    var firstLink = cellNode(upload, 0, 0, "testCase");
    assertNotNull(firstLink);
    assertEquals(libraryIds.getFirst(), firstLink.get("value").asText());
    assertEquals(libraryIds.getFirst(), firstLink.get("target").get("entityId").asText());
    assertEquals("2", attribute(upload, "autoReadyPromoted"));
    assertEquals("90", attribute(upload, "autoReadyThreshold"));
    assertFalse(detail.has("warnings") && !detail.get("warnings").isEmpty(), "every case exists in the Library");

    assertEquals("READY", libraryCase(0).get("status").asText(), "94 >= 90");
    assertEquals("READY", libraryCase(1).get("status").asText(), "91 >= 90");
    assertEquals("DRAFT", libraryCase(2).get("status").asText(), "72 < 90");
    assertEquals("DRAFT", libraryCase(3).get("status").asText(), "58 < 90");

    var review = stage(detail, "Review");
    assertStage(review, "In progress", "WAITING", "REPORTPORTAL", true);
    assertEquals("2/4 Ready", review.get("summary").asText());
    assertEquals("2/4", attribute(review, "review"));
    assertEquals("2", attribute(review, "ready"));
    assertEquals("4", attribute(review, "total"));
    assertEquals("2 Draft Test Cases wait for review in the Test Case Library", review.get("message").asText());
    assertNotNull(review.get("startedAt"));

    assertIterationStatus(detail, "In review", "WAITING");
    assertEquals("2", attribute(detail, "readyNow"));
    assertEquals("2", attribute(detail, "autoReadyPromoted"));
    assertEquals("4", attribute(detail, "testCases"));
    assertMoney("1.27", attribute(detail, "cost"));
  }

  // ------------------------------------------------------------------ steps 13-15

  private void step13_libraryIdsGoBackIntoCreateAndGradeTables() throws Exception {
    var create = stage(reportStage("create", createStageBody(true)), "Create");
    var grade = stage(reportStage("grade", gradeStageBody(true)), "Grade");

    for (int i = 0; i < 4; i++) {
      var libraryId = cellNode(create, 0, i, "libraryId");
      assertNotNull(libraryId);
      assertEquals(libraryIds.get(i), libraryId.get("value").asText());
      assertEquals("TEST_CASE", libraryId.get("target").get("entityType").asText());
      assertEquals(libraryIds.get(i), libraryId.get("target").get("entityId").asText());
      assertEquals(libraryIds.get(i), Objects.requireNonNull(cellNode(grade, 0, i, "testCase")).get("target").get("entityId").asText());
    }
    assertEquals(1, create.get("attempt").asInt(), "the same job: the current attempt is updated");
    assertEquals(1, grade.get("attempt").asInt());

    var detail = iteration();
    assertIterationStatus(detail, "In review", "WAITING");
    assertStage(stage(detail, "Upload"), "Passed", "PASSED", "PIPELINE", true);
    assertEquals("2/4", attribute(stage(detail, "Review"), "review"), "no status changed, nothing was promoted twice");
  }

  private void step14_ciPipelineEnds() throws Exception {
    var detail = send(patch(PIPELINES + "/iteration/" + iterationId), "{\"finishedAt\": \"2026-09-19T10:22:40Z\"}",
        HttpStatus.OK);

    assertEquals(580000, detail.get("durationMillis").asLong());
    assertIterationStatus(detail, "In review", "WAITING");
  }

  private void step15_whatTheUiReadsNow() throws Exception {
    var page = send(get(PIPELINES), null, HttpStatus.OK);
    var pipeline = page.get("content").get(0);
    assertEquals(90, pipeline.get("settings").get("autoReady").get("threshold").asInt());
    assertEquals(1, pipeline.get("iterationsCount").asInt());

    var cards = send(get(PIPELINES + "/" + pipelineId + "/iteration"), null, HttpStatus.OK);
    var latest = cards.get("content").get(0);
    assertEquals(iterationId, latest.get("id").asLong());
    assertIterationStatus(latest, "In review", "WAITING");
    assertEquals(580000, latest.get("durationMillis").asLong());
    assertEquals("2", attribute(latest, "readyNow"));

    var detail = iteration();
    assertIterationStatus(detail, "In review", "WAITING");
    var linkedCreateRow = cellNode(stage(detail, "Create"), 0, 2, "libraryId");
    assertNotNull(linkedCreateRow);
    assertEquals(libraryIds.get(2), linkedCreateRow.get("target").get("entityId").asText());
    assertEquals(580000, detail.get("durationMillis").asLong());
  }

  // ------------------------------------------------------------------ the editor finishes the review

  private void editorMarksTheOtherTwoCasesReady_IterationCompletes() throws Exception {
    send(patch(TEST_CASES + "/" + libraryEntityIds.get(2)), "{\"status\": \"READY\"}", HttpStatus.OK);

    var afterThird = iteration();
    assertIterationStatus(afterThird, "In review", "WAITING");
    assertEquals("3/4", attribute(stage(afterThird, "Review"), "review"));
    assertEquals("1 Draft Test Case waits for review in the Test Case Library",
        stage(afterThird, "Review").get("message").asText());

    send(patch(TEST_CASES + "/" + libraryEntityIds.get(3)), "{\"status\": \"READY\"}", HttpStatus.OK);

    var done = iteration();
    var review = stage(done, "Review");
    assertStage(review, "Done", "PASSED", "REPORTPORTAL", true);
    assertEquals("4/4", attribute(review, "review"));
    assertNotNull(review.get("finishedAt"));
    assertIterationStatus(done, "Completed", "PASSED");
    assertEquals("4", attribute(done, "readyNow"));

    // and back: a case returned to Draft reopens the review
    send(patch(TEST_CASES + "/" + libraryEntityIds.get(3)), "{\"status\": \"DRAFT\"}", HttpStatus.OK);
    var reopened = iteration();
    assertIterationStatus(reopened, "In review", "WAITING");
    assertEquals("3/4", attribute(stage(reopened, "Review"), "review"));
  }

  // ------------------------------------------------------------------ request bodies

  private static String execution(String jobId) {
    return """
        {"executorType": "GITLAB_CI", "externalRunId": "%s", "runUrl": "%s%s", "retryable": true,
         "retryParameters": {"repo": "epm-rpp/rp-tests", "workflowRef": "main"}}
        """.formatted(jobId, RUN_URL, jobId);
  }

  /**
   * The Create report of step 6 (the table has no Library ids) or of step 13 (the same report, with the Library id of
   * every case as a link cell).
   */
  private String createStageBody(boolean withLibraryIds) {
    var body = mapper.createObjectNode();
    body.put("name", "Create").put("status", "Passed").put("startedAt", "2026-09-19T10:13:00Z")
        .put("finishedAt", "2026-09-19T10:19:02Z").put("message", "4 Test Cases authored").put("summary", "4 cases");
    var attributes = body.putArray("attributes");
    attributes.add(attribute("testCases", "Test Cases", "4", "META", "NUMBER").put("order", 1).put("rollup", "SUM"));
    attributes.add(attribute("cost", "Cost", "0.94", "NONE", "MONEY").put("direction", "LOWER_BETTER")
        .put("rollup", "SUM"));
    attributes.add(attribute("model", "Model", "auto (default)", "NONE", "TEXT"));
    attributes.add(attribute("note", "Note", "self-review and self-fix included", "META", "TEXT"));

    var sections = body.putArray("sections");
    var cases = sections.addObject().put("key", "authoredCases").put("title", "Authored test cases (child steps)")
        .put("role", "RESULT");
    var columns = cases.putArray("columns");
    column(columns, "name", "Child step · authored test case", null);
    column(columns, "priority", "Priority", null);
    column(columns, "status", "Step status", "STATUS");
    column(columns, "libraryId", "Library id (after Upload)", null);
    var rows = cases.putArray("rows");
    for (int i = 0; i < CASES.size(); i++) {
      var cells = rows.addObject().put("key", "case-" + (i + 1)).putObject("cells");
      cells.put("name", CASES.get(i).name()).put("priority", CASES.get(i).priority()).put("status", "Passed");
      if (withLibraryIds) {
        putLink(cells, "libraryId", libraryIds.get(i));
      }
    }
    sections.add(tokenUsage(318400, 2100000, 88200, 41200, "0.94"));
    body.set("execution", executionNode("8812301"));
    return body.toString();
  }

  /** The Grade report of step 8 (no Library ids yet) or of step 13 (with the "test case" link on every row). */
  private String gradeStageBody(boolean withLibraryIds) {
    var body = mapper.createObjectNode();
    body.put("name", "Grade").put("status", "Passed").put("affectsIterationStatus", false)
        .put("startedAt", "2026-09-19T10:19:10Z").put("finishedAt", "2026-09-19T10:21:41Z")
        .put("message", "4 cases graded").put("summary", "79").put("rawOutputSchemaName", "grade");
    var attributes = body.putArray("attributes");
    attributes.add(attribute("suiteScore", "Suite score", "79", "META", "SCORE").put("order", 1)
        .put("direction", "HIGHER_BETTER"));
    attributes.add(attribute("cost", "Cost", "0.31", "NONE", "MONEY").put("direction", "LOWER_BETTER")
        .put("rollup", "SUM"));
    attributes.add(attribute("model", "Model", "auto (default)", "NONE", "TEXT"));

    var sections = body.putArray("sections");
    var graded = sections.addObject().put("key", "gradedCases").put("title", "Graded test cases").put("role", "RESULT");
    var columns = graded.putArray("columns");
    column(columns, "testCase", "Test case", null);
    column(columns, "name", "Name", null);
    for (int i = 0; i < CRITERIA.length; i++) {
      column(columns, camel(CRITERIA[i]), CRITERION_LABELS[i] + " /" + CRITERION_MAX[i], "SCORE");
    }
    column(columns, "total", "TOTAL /100", "SCORE");
    var rows = graded.putArray("rows");
    var rawOutput = body.putObject("rawOutput");
    rawOutput.put("total_score", 79).putArray("warnings");
    rawOutput.putNull("error");
    var rawOutputCases = rawOutput.putArray("cases");
    for (int i = 0; i < CASES.size(); i++) {
      var authored = CASES.get(i);
      var row = rows.addObject().put("key", "case-" + (i + 1));
      var cells = row.putObject("cells");
      if (withLibraryIds) {
        putLink(cells, "testCase", libraryIds.get(i));
      }
      cells.put("name", authored.name());
      var details = row.putArray("details");
      var rawOutputCase = rawOutputCases.addObject().put("name", authored.name()).put("total_score", authored.total());
      var criteria = rawOutputCase.putArray("criteria");
      for (int c = 0; c < CRITERIA.length; c++) {
        cells.put(camel(CRITERIA[c]), authored.scores()[c]);
        var reasons = criteria.addObject().put("name", CRITERIA[c]).put("score", authored.scores()[c])
            .put("max_score", CRITERION_MAX[c]).putArray("failure_reasons");
        if (authored.reasons()[c] != null) {
          reasons.add(authored.reasons()[c]);
          var detail = details.addObject();
          detail.put("heading", CRITERION_LABELS[c] + " " + authored.scores()[c] + "/" + CRITERION_MAX[c]);
          detail.putArray("items").add(authored.reasons()[c]);
        }
      }
      cells.put("total", authored.total());
    }
    sections.add(tokenUsage(96200, 612000, 21400, 12800, "0.31"));
    body.set("execution", executionNode("8812317"));
    return body.toString();
  }

  /** The Upload report of step 12: the Library id of each case is the {@code target} of the "test case" cell. */
  private String uploadStageBody() {
    var body = mapper.createObjectNode();
    body.put("name", "Upload").put("status", "Passed").put("startedAt", "2026-09-19T10:21:50Z")
        .put("finishedAt", "2026-09-19T10:22:04Z").put("message", "4 uploaded as Draft").put("summary", "4 ↑");
    var attributes = body.putArray("attributes");
    attributes.add(attribute("uploaded", "uploaded", "4", "META", "NUMBER").put("order", 1));
    attributes.add(attribute("cost", "Cost", "0.02", "NONE", "MONEY").put("direction", "LOWER_BETTER")
        .put("rollup", "SUM"));
    attributes.add(attribute("model", "Model", "auto (default)", "NONE", "TEXT"));

    var sections = body.putArray("sections");
    var report = sections.addObject().put("key", "uploadReport").put("title", "Upload report").put("role", "RESULT");
    var columns = report.putArray("columns");
    column(columns, "testCase", "Test case", null);
    column(columns, "name", "Name", null);
    column(columns, "score", "Score", "SCORE");
    column(columns, "result", "Result", null);
    var rows = report.putArray("rows");
    for (int i = 0; i < CASES.size(); i++) {
      var cells = rows.addObject().put("key", "case-" + (i + 1)).putObject("cells");
      putLink(cells, "testCase", libraryIds.get(i));
      cells.put("name", CASES.get(i).name()).put("score", CASES.get(i).total()).put("result", "Created");
    }
    sections.add(tokenUsage(8200, 31000, 0, 900, "0.02"));
    body.set("execution", executionNode("8812333"));
    return body.toString();
  }

  private ObjectNode executionNode(String jobId) {
    try {
      return (ObjectNode) mapper.readTree(execution(jobId));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private ObjectNode tokenUsage(int input, int cacheRead, int cacheWrite, int output, String cost) {
    var section = mapper.createObjectNode().put("key", "tokenUsage").put("title", "Token usage")
        .put("role", "DETAILS");
    var columns = section.putArray("columns");
    column(columns, "model", "Model", null);
    column(columns, "input", "Input", "NUMBER");
    column(columns, "cacheRead", "Cache read", "NUMBER");
    column(columns, "cacheWrite", "Cache write", "NUMBER");
    column(columns, "output", "Output", "NUMBER");
    column(columns, "cost", "Est. cost", "MONEY");
    section.putArray("rows").addObject().put("key", "auto").putObject("cells").put("model", "auto (default)")
        .put("input", input).put("cacheRead", cacheRead).put("cacheWrite", cacheWrite).put("output", output)
        .put("cost", new BigDecimal(cost));
    return section;
  }

  private ObjectNode attribute(String key, String label, String value, String type, String valueType) {
    return mapper.createObjectNode().put("key", key).put("label", label).put("value", value).put("type", type)
        .put("valueType", valueType);
  }

  private static void column(ArrayNode columns, String key, String label, String valueType) {
    var column = columns.addObject().put("key", key).put("label", label);
    if (valueType != null) {
      column.put("valueType", valueType);
    }
  }

  private static void putLink(ObjectNode cells, String column, String libraryId) {
    var cell = cells.putObject(column);
    cell.put("value", libraryId);
    cell.putObject("target").put("entityType", "TEST_CASE").put("entityId", libraryId);
  }

  private static String camel(String snake) {
    var parts = snake.split("_");
    var result = new StringBuilder(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      result.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
    }
    return result.toString();
  }

  // ------------------------------------------------------------------ calls and reading

  private JsonNode reportStage(String stageKey, String body) throws Exception {
    return send(put(PIPELINES + "/iteration/" + iterationId + "/stage/" + stageKey), body, HttpStatus.OK);
  }

  private JsonNode iteration() throws Exception {
    return send(get(PIPELINES + "/iteration/" + iterationId), null, HttpStatus.OK);
  }

  private JsonNode libraryCase(int index) throws Exception {
    return send(get(TEST_CASES + "/" + libraryEntityIds.get(index)), null, HttpStatus.OK);
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

  private static JsonNode attributeNode(JsonNode owner, String key) {
    for (var attribute : owner.path("attributes")) {
      if (key.equals(attribute.path("key").asText())) {
        return attribute;
      }
    }
    return null;
  }

  private static String attribute(JsonNode owner, String key) {
    var attribute = attributeNode(owner, key);
    return attribute == null ? null : attribute.path("value").asText();
  }

  private static JsonNode cellNode(JsonNode stage, int section, int row, String column) {
    var cell = stage.path("sections").path(section).path("rows").path(row).path("cells").path(column);
    return cell.isMissingNode() ? null : cell;
  }

  private static String cell(JsonNode stage, int section, int row, String column) {
    var cell = cellNode(stage, section, row, column);
    return cell == null ? null : cell.path("value").asText();
  }

  private static void assertIterationStatus(JsonNode iteration, String status, String category) {
    assertEquals(status, iteration.get("status").asText(), "iteration status");
    assertEquals(category, iteration.get("statusCategory").asText(), "iteration status category");
  }

  private static void assertStage(JsonNode stage, String status, String category, String ownedBy, boolean counts) {
    var name = stage.get("name").asText();
    assertEquals(status, stage.get("status").asText(), name + " status");
    assertEquals(category, stage.get("statusCategory").asText(), name + " status category");
    assertEquals(ownedBy, stage.get("ownedBy").asText(), name + " owner");
    assertEquals(counts, stage.get("affectsIterationStatus").asBoolean(), name + " counts towards the iteration");
  }

  private static void assertMoney(String expected, String actual) {
    assertNotNull(actual);
    assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual)),
        "expected " + expected + " but was " + actual);
  }
}
