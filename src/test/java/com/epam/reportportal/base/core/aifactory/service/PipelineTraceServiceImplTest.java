package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceRQ;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.enums.TraceEventType;
import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTraceEventRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTraceRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTrace;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTraceEvent;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PipelineTraceServiceImplTest {

  private static final Long PROJECT_ID = 1L;
  private static final Long ITERATION_ID = 10L;
  private static final String STAGE_KEY = "create";

  @Mock
  private PipelineIterationRepository pipelineIterationRepository;
  @Mock
  private PipelineStageRepository pipelineStageRepository;
  @Mock
  private PipelineStageAttemptRepository pipelineStageAttemptRepository;
  @Mock
  private PipelineStageTraceRepository traceRepository;
  @Mock
  private PipelineStageTraceEventRepository eventRepository;

  private PipelineTraceServiceImpl sut;
  private PipelineStage stage;

  @BeforeEach
  void setUp() {
    sut = new PipelineTraceServiceImpl(pipelineIterationRepository, pipelineStageRepository,
        pipelineStageAttemptRepository, traceRepository, eventRepository);

    var iteration = mock(PipelineIteration.class, RETURNS_DEEP_STUBS);
    when(iteration.getId()).thenReturn(ITERATION_ID);
    when(iteration.getPipeline().getProject().getId()).thenReturn(PROJECT_ID);
    when(pipelineIterationRepository.findByIdForUpdate(ITERATION_ID)).thenReturn(Optional.of(iteration));
    when(pipelineIterationRepository.findById(ITERATION_ID)).thenReturn(Optional.of(iteration));

    stage = new PipelineStage();
    stage.setStageKey(STAGE_KEY);
    stage.setAttempt(2);
    stage.setStatus("Passed");
    stage.setStatusCategory(StatusCategory.PASSED);
    when(pipelineStageRepository.findByIterationIdAndStageKey(ITERATION_ID, STAGE_KEY)).thenReturn(Optional.of(stage));
    when(traceRepository.findByIterationIdAndStageKeyAndAttempt(any(), any(), org.mockito.ArgumentMatchers.anyInt()))
        .thenReturn(Optional.empty());
  }

  private static PipelineTraceRQ.Event event(int seq, TraceEventType type, String text) {
    return PipelineTraceRQ.Event.builder().seq(seq).type(type).title("t" + seq).text(text).build();
  }

  @Test
  void reportTrace_ShouldStoreEventsWithFullText() {
    var longText = "x".repeat(300_000);
    var rq = PipelineTraceRQ.builder().attempt(2)
        .events(List.of(event(0, TraceEventType.PROMPT, longText), event(1, TraceEventType.FINAL, "done")))
        .build();

    sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PipelineStageTraceEvent>> saved = ArgumentCaptor.forClass(List.class);
    verify(eventRepository).saveAll(saved.capture());
    assertEquals(2, saved.getValue().size());
    assertEquals(longText, saved.getValue().get(0).getText());
    assertEquals(2, saved.getValue().get(0).getAttempt());
    assertEquals(TraceEventType.FINAL, saved.getValue().get(1).getType());
    var trace = ArgumentCaptor.forClass(PipelineStageTrace.class);
    verify(traceRepository).save(trace.capture());
    assertEquals(TraceStatus.REPORTED, trace.getValue().getStatus());
  }

  @Test
  void reportTrace_ShouldStoreTheUnitKeyOfAnEventAndLeaveItEmptyForTheStageItself() {
    var unitEvent = PipelineTraceRQ.Event.builder().seq(1).type(TraceEventType.AGENT)
        .unitKey("test-case-review-single#2#1").text("reviewing TC-12").build();
    var rq = PipelineTraceRQ.builder().attempt(2)
        .events(List.of(event(0, TraceEventType.PROMPT, "orchestrator prompt"), unitEvent)).build();

    sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PipelineStageTraceEvent>> saved = ArgumentCaptor.forClass(List.class);
    verify(eventRepository).saveAll(saved.capture());
    assertNull(saved.getValue().get(0).getUnitKey());
    assertEquals("test-case-review-single#2#1", saved.getValue().get(1).getUnitKey());
  }

  @Test
  void getTrace_ShouldReturnTheUnitKeyOfAnEvent() {
    var unitEvent = new PipelineStageTraceEvent();
    unitEvent.setSeq(0);
    unitEvent.setType(TraceEventType.AGENT);
    unitEvent.setUnitKey("test-case-review-single#2#1");
    when(eventRepository.findByIterationIdAndStageKeyAndAttemptOrderBySeqAsc(ITERATION_ID, STAGE_KEY, 1))
        .thenReturn(List.of(unitEvent));

    var result = sut.getTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, 1);

    assertEquals("test-case-review-single#2#1", result.getEvents().get(0).getUnitKey());
  }

  @Test
  void reportTrace_WhenSeqAlreadyStored_ShouldReplaceItInsteadOfDuplicating() {
    var stored = new PipelineStageTraceEvent();
    stored.setSeq(0);
    stored.setText("old");
    when(eventRepository.findByIterationIdAndStageKeyAndAttemptAndSeqIn(any(), any(), org.mockito.ArgumentMatchers.anyInt(),
        any())).thenReturn(List.of(stored));
    var rq = PipelineTraceRQ.builder().attempt(2).events(List.of(event(0, TraceEventType.AGENT, "new"))).build();

    sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<PipelineStageTraceEvent>> saved = ArgumentCaptor.forClass(List.class);
    verify(eventRepository).saveAll(saved.capture());
    assertEquals(1, saved.getValue().size());
    assertSame(stored, saved.getValue().get(0));
    assertEquals("new", stored.getText());
    assertEquals(TraceEventType.AGENT, stored.getType());
  }

  @Test
  void reportTrace_WhenAttemptBeyondCurrent_ShouldBeRejected() {
    var rq = PipelineTraceRQ.builder().attempt(3).events(List.of(event(0, TraceEventType.PROMPT, "p"))).build();

    var ex = assertThrows(ReportPortalException.class, () -> sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq));

    assertTrue(ex.getMessage().contains("no attempt 3"));
    verify(eventRepository, never()).saveAll(any());
  }

  @Test
  void reportTrace_WhenSeqRepeatedInOneRequest_ShouldBeRejected() {
    var rq = PipelineTraceRQ.builder().attempt(1)
        .events(List.of(event(0, TraceEventType.PROMPT, "a"), event(0, TraceEventType.AGENT, "b"))).build();

    assertThrows(ReportPortalException.class, () -> sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq));

    verify(eventRepository, never()).saveAll(any());
  }

  @Test
  void reportTrace_WhenOtherProject_ShouldNotFindIteration() {
    var rq = PipelineTraceRQ.builder().attempt(1).events(List.of(event(0, TraceEventType.PROMPT, "a"))).build();

    var ex = assertThrows(ReportPortalException.class, () -> sut.reportTrace(99L, ITERATION_ID, STAGE_KEY, rq));

    assertEquals(ErrorType.NOT_FOUND.getCode(), ex.getErrorType().getCode());
  }

  @Test
  void reportTrace_WhenNotReported_ShouldKeepReasonAndNoEvents() {
    var rq = PipelineTraceRQ.builder().attempt(2).status(TraceStatus.NOT_REPORTED).reason("agent output was empty")
        .build();

    sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq);

    var trace = ArgumentCaptor.forClass(PipelineStageTrace.class);
    verify(traceRepository).save(trace.capture());
    assertEquals(TraceStatus.NOT_REPORTED, trace.getValue().getStatus());
    assertEquals("agent output was empty", trace.getValue().getNotReportedReason());
    verify(eventRepository, never()).saveAll(any());
  }

  @Test
  void reportTrace_WhenNotReportedAfterReported_ShouldKeepTheReportedTrace() {
    var existing = new PipelineStageTrace();
    existing.setStatus(TraceStatus.REPORTED);
    when(traceRepository.findByIterationIdAndStageKeyAndAttempt(ITERATION_ID, STAGE_KEY, 2))
        .thenReturn(Optional.of(existing));
    var rq = PipelineTraceRQ.builder().attempt(2).status(TraceStatus.NOT_REPORTED).reason("late failure").build();

    sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq);

    verify(traceRepository, never()).save(any());
    assertEquals(TraceStatus.REPORTED, existing.getStatus());
  }

  @Test
  void reportTrace_WhenNotReportedCarriesEvents_ShouldBeRejected() {
    var rq = PipelineTraceRQ.builder().attempt(2).status(TraceStatus.NOT_REPORTED)
        .events(List.of(event(0, TraceEventType.PROMPT, "a"))).build();

    assertThrows(ReportPortalException.class, () -> sut.reportTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, rq));
  }

  @Test
  void getTrace_WhenNoTraceWasReported_ShouldBeNotReportedWithoutEvents() {
    var result = sut.getTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, null);

    assertEquals(2, result.getAttempt());
    assertEquals(TraceStatus.NOT_REPORTED, result.getStatus());
    assertTrue(result.getEvents().isEmpty());
    assertNull(result.getReportedAt());
  }

  @Test
  void getTrace_ShouldReturnEventsInOrderWithTextAndData() {
    var trace = new PipelineStageTrace();
    trace.setStatus(TraceStatus.REPORTED);
    trace.setReportedAt(Instant.parse("2026-11-17T10:00:00Z"));
    when(traceRepository.findByIterationIdAndStageKeyAndAttempt(ITERATION_ID, STAGE_KEY, 1))
        .thenReturn(Optional.of(trace));
    var graders = new PipelineStageTraceEvent();
    graders.setSeq(0);
    graders.setType(TraceEventType.GRADERS);
    graders.setText("72 -> 88 / 100");
    graders.setData(new com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineRawOutput(
        Map.of("total", 88)));
    when(eventRepository.findByIterationIdAndStageKeyAndAttemptOrderBySeqAsc(ITERATION_ID, STAGE_KEY, 1))
        .thenReturn(List.of(graders));

    var result = sut.getTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, 1);

    assertEquals(TraceStatus.REPORTED, result.getStatus());
    assertEquals(1, result.getEvents().size());
    assertEquals("72 -> 88 / 100", result.getEvents().get(0).getText());
    assertEquals(88, result.getEvents().get(0).getData().get("total"));
  }

  @Test
  void getTrace_WhenAttemptDoesNotExist_ShouldBeNotFound() {
    assertThrows(ReportPortalException.class, () -> sut.getTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, 3));
    assertThrows(ReportPortalException.class, () -> sut.getTrace(PROJECT_ID, ITERATION_ID, STAGE_KEY, 0));
  }

  @Test
  void getTraceAttempts_ShouldListFinishedAttemptsThenTheCurrentOne() {
    var first = new PipelineStageAttempt();
    first.setAttempt(1);
    first.setStatus("Failed");
    first.setStatusCategory(StatusCategory.FAILED);
    when(pipelineStageAttemptRepository.findByIterationIdAndStageKeyOrderByAttemptAsc(ITERATION_ID, STAGE_KEY))
        .thenReturn(List.of(first));
    var trace = new PipelineStageTrace();
    trace.setAttempt(2);
    trace.setStatus(TraceStatus.REPORTED);
    when(traceRepository.findByIterationIdAndStageKeyOrderByAttemptAsc(ITERATION_ID, STAGE_KEY))
        .thenReturn(List.of(trace));
    var count = mock(PipelineStageTraceEventRepository.AttemptEventCount.class);
    when(count.getAttempt()).thenReturn(2);
    when(count.getCount()).thenReturn(7L);
    when(eventRepository.countByAttempt(ITERATION_ID, STAGE_KEY)).thenReturn(List.of(count));

    var result = sut.getTraceAttempts(PROJECT_ID, ITERATION_ID, STAGE_KEY);

    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getAttempt());
    assertEquals(TraceStatus.NOT_REPORTED, result.get(0).getTraceStatus());
    assertEquals(0, result.get(0).getEventCount());
    assertEquals(2, result.get(1).getAttempt());
    assertTrue(result.get(1).isCurrent());
    assertEquals(TraceStatus.REPORTED, result.get(1).getTraceStatus());
    assertEquals(7, result.get(1).getEventCount());
  }
}
