package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.aifactory.dto.IterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceRQ;
import com.epam.reportportal.base.core.aifactory.enums.TraceEventType;
import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTraceEventRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTraceRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

/**
 * The trace of a stage attempt over the real controller, service and database: reported by the CI job in its own
 * request, repeated without duplicates, read back in full, and deleted with its iteration.
 */
@Sql("/db/aifactory/pipeline/pipeline-fill.sql")
@Sql(scripts = "/db/aifactory/pipeline/pipeline-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PipelineTraceIntegrationTest extends BaseMvcTest {

  private static final String BASE = "/v1/project/superadmin_personal/pipeline";

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private PipelineStageTraceRepository traceRepository;
  @Autowired
  private PipelineStageTraceEventRepository eventRepository;

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  private long iterationWithFinishedCreateStage() throws Exception {
    var created = mapper.readTree(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineIterationRQ.builder().name("traced")
                .iteration(IterationRQ.builder().build()).build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString());
    var iterationId = created.get("id").asLong();
    mockMvc.perform(put(BASE + "/iteration/" + iterationId + "/stage/create").contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineStageRQ.builder().status("Passed").build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk());
    return iterationId;
  }

  private void report(long iterationId, PipelineTraceRQ rq) throws Exception {
    mockMvc.perform(put(BASE + "/iteration/" + iterationId + "/stage/create/trace").contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNoContent());
  }

  private static PipelineTraceRQ.Event event(int seq, TraceEventType type, String text) {
    return PipelineTraceRQ.Event.builder().seq(seq).type(type).title("event " + seq).text(text).build();
  }

  @Test
  void trace_ShouldBeReportedInPartsRepeatedWithoutDuplicatesAndReadBackInFull() throws Exception {
    var iterationId = iterationWithFinishedCreateStage();
    var longText = "line of the agent output\n".repeat(20_000);
    var grader = PipelineTraceRQ.Event.builder().seq(2).type(TraceEventType.GRADERS).title("TC-12")
        .data(Map.of("total", 88)).build();

    report(iterationId, PipelineTraceRQ.builder().attempt(1)
        .events(List.of(event(0, TraceEventType.PROMPT, longText), event(1, TraceEventType.AGENT, "reasoning")))
        .build());
    report(iterationId, PipelineTraceRQ.builder().attempt(1).events(List.of(grader)).build());
    // the first part again, with a corrected text for seq 1
    report(iterationId, PipelineTraceRQ.builder().attempt(1)
        .events(List.of(event(0, TraceEventType.PROMPT, longText), event(1, TraceEventType.AGENT, "corrected")))
        .build());

    var trace = mapper.readTree(mockMvc.perform(get(BASE + "/iteration/" + iterationId + "/stage/create/trace")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REPORTED"))
        .andExpect(jsonPath("$.attempt").value(1))
        .andExpect(jsonPath("$.events.length()").value(3))
        .andExpect(jsonPath("$.events[0].type").value("PROMPT"))
        .andExpect(jsonPath("$.events[1].text").value("corrected"))
        .andExpect(jsonPath("$.events[2].type").value("GRADERS"))
        .andExpect(jsonPath("$.events[2].data.total").value(88))
        .andReturn().getResponse().getContentAsString());
    assertEquals(longText, trace.get("events").get(0).get("text").asText());

    mockMvc.perform(get(BASE + "/iteration/" + iterationId + "/stage/create/trace/attempts")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].traceStatus").value("REPORTED"))
        .andExpect(jsonPath("$[0].eventCount").value(3))
        .andExpect(jsonPath("$[0].current").value(true));

    // the stage panel and the iteration never carry event texts
    mockMvc.perform(get(BASE + "/iteration/" + iterationId).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stages[0].events").doesNotExist());
  }

  @Test
  void trace_ShouldKeepTheUnitKeyOfEachEventSoTheEventsOfSubAgentsCanBeToldApart() throws Exception {
    var iterationId = iterationWithFinishedCreateStage();
    var firstUnit = PipelineTraceRQ.Event.builder().seq(1).type(TraceEventType.AGENT)
        .unitKey("test-case-review-single#2#1").text("reviewing TC-12").build();
    var secondUnit = PipelineTraceRQ.Event.builder().seq(2).type(TraceEventType.AGENT)
        .unitKey("test-case-review-single#2#2").text("reviewing TC-13").build();

    report(iterationId, PipelineTraceRQ.builder().attempt(1)
        .events(List.of(event(0, TraceEventType.PROMPT, "orchestrator"), firstUnit, secondUnit)).build());

    mockMvc.perform(get(BASE + "/iteration/" + iterationId + "/stage/create/trace")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events[0].unitKey").doesNotExist())
        .andExpect(jsonPath("$.events[1].unitKey").value("test-case-review-single#2#1"))
        .andExpect(jsonPath("$.events[2].unitKey").value("test-case-review-single#2#2"));
  }

  @Test
  void trace_WhenTheJobCouldNotReportIt_ShouldShowNotReportedWithTheReason() throws Exception {
    var iterationId = iterationWithFinishedCreateStage();

    report(iterationId, PipelineTraceRQ.builder().attempt(1).status(TraceStatus.NOT_REPORTED)
        .reason("agent output was empty").build());

    mockMvc.perform(get(BASE + "/iteration/" + iterationId + "/stage/create/trace")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("NOT_REPORTED"))
        .andExpect(jsonPath("$.reason").value("agent output was empty"))
        .andExpect(jsonPath("$.events.length()").value(0));
  }

  @Test
  void trace_ShouldBeDeletedTogetherWithItsIteration() throws Exception {
    var iterationId = iterationWithFinishedCreateStage();
    report(iterationId, PipelineTraceRQ.builder().attempt(1)
        .events(List.of(event(0, TraceEventType.PROMPT, "p"))).build());
    assertEquals(1, eventRepository.findByIterationIdAndStageKeyAndAttemptOrderBySeqAsc(iterationId, "create", 1)
        .size());

    // plain SQL: this checks the database cascade, which is what removes a trace with its iteration
    jdbcTemplate.update("delete from pipeline_iteration where id = ?", iterationId);

    assertEquals(0, eventRepository.findByIterationIdAndStageKeyAndAttemptOrderBySeqAsc(iterationId, "create", 1)
        .size());
    assertEquals(0, traceRepository.findByIterationIdAndStageKeyOrderByAttemptAsc(iterationId, "create").size());
  }

  @Test
  void trace_OfAnAttemptTheStageDoesNotHave_ShouldBeRejected() throws Exception {
    var iterationId = iterationWithFinishedCreateStage();

    mockMvc.perform(put(BASE + "/iteration/" + iterationId + "/stage/create/trace").contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineTraceRQ.builder().attempt(2)
                .events(List.of(event(0, TraceEventType.PROMPT, "p"))).build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }
}
