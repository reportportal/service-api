package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceAttemptRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineTraceRS;
import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTraceEventRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTraceRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineRawOutput;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTrace;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTraceEvent;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PipelineTraceServiceImpl implements PipelineTraceService {

  /** A request carries a part of a trace; larger attempts are sent in several requests. */
  static final long MAX_TRACE_REQUEST_BYTES = 50L * 1024 * 1024;

  private final PipelineIterationRepository pipelineIterationRepository;
  private final PipelineStageRepository pipelineStageRepository;
  private final PipelineStageAttemptRepository pipelineStageAttemptRepository;
  private final PipelineStageTraceRepository traceRepository;
  private final PipelineStageTraceEventRepository eventRepository;

  @Override
  @Transactional
  public void reportTrace(Long projectId, Long iterationId, String stageKey, PipelineTraceRQ rq) {
    // The iteration lock serializes concurrent parts of one attempt, so they cannot race on the unique keys.
    var iteration = pipelineIterationRepository.findByIdForUpdate(iterationId)
        .filter(it -> it.getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline iteration '" + iterationId + "'"));
    var stage = findStage(iteration.getId(), stageKey);
    int attempt = rq.getAttempt();
    if (attempt > stage.getAttempt()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Stage '" + stageKey + "' has "
          + stage.getAttempt() + " attempt(s), there is no attempt " + attempt + " to report a trace for");
    }
    var events = rq.getEvents() == null ? List.<PipelineTraceRQ.Event>of() : rq.getEvents();
    if (rq.getStatus() == TraceStatus.NOT_REPORTED && !events.isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "A trace that is not reported carries no events");
    }
    if (IngestFingerprint.sizeInBytes(rq) > MAX_TRACE_REQUEST_BYTES) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Trace request is larger than " + MAX_TRACE_REQUEST_BYTES / (1024 * 1024)
              + " MB; send the events in several requests");
    }
    var seqs = new HashSet<Integer>();
    for (var event : events) {
      if (!seqs.add(event.getSeq())) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Event seq " + event.getSeq() + " is reported twice in one request");
      }
    }

    var trace = traceRepository.findByIterationIdAndStageKeyAndAttempt(iteration.getId(), stageKey, attempt)
        .orElseGet(() -> {
          var created = new PipelineStageTrace();
          created.setIterationId(iteration.getId());
          created.setStageKey(stageKey);
          created.setAttempt(attempt);
          return created;
        });
    // A job that reports "not reported" never wipes events that arrived earlier.
    if (rq.getStatus() == TraceStatus.NOT_REPORTED && trace.getStatus() == TraceStatus.REPORTED) {
      return;
    }
    trace.setStatus(rq.getStatus() == null ? TraceStatus.REPORTED : rq.getStatus());
    trace.setNotReportedReason(trace.getStatus() == TraceStatus.NOT_REPORTED ? rq.getReason() : null);
    trace.setReportedAt(Instant.now());
    traceRepository.save(trace);

    if (events.isEmpty()) {
      return;
    }
    Map<Integer, PipelineStageTraceEvent> stored = eventRepository
        .findByIterationIdAndStageKeyAndAttemptAndSeqIn(iteration.getId(), stageKey, attempt, seqs).stream()
        .collect(Collectors.toMap(PipelineStageTraceEvent::getSeq, Function.identity()));
    var toSave = new ArrayList<PipelineStageTraceEvent>(events.size());
    for (var event : events) {
      var entity = stored.get(event.getSeq());
      if (entity == null) {
        entity = new PipelineStageTraceEvent();
        entity.setIterationId(iteration.getId());
        entity.setStageKey(stageKey);
        entity.setAttempt(attempt);
        entity.setSeq(event.getSeq());
      }
      entity.setType(event.getType());
      entity.setTitle(event.getTitle());
      entity.setOffsetMs(event.getOffsetMs());
      entity.setDurationMs(event.getDurationMs());
      entity.setTarget(event.getTarget());
      entity.setText(event.getText());
      entity.setData(event.getData() == null ? null : new PipelineRawOutput(event.getData()));
      toSave.add(entity);
    }
    eventRepository.saveAll(toSave);
  }

  @Override
  @Transactional(readOnly = true)
  public PipelineTraceRS getTrace(Long projectId, Long iterationId, String stageKey, Integer requestedAttempt) {
    var stage = findStageOfProject(projectId, iterationId, stageKey);
    int attempt = requestedAttempt == null ? stage.getAttempt() : requestedAttempt;
    if (attempt < 1 || attempt > stage.getAttempt()) {
      throw new ReportPortalException(ErrorType.NOT_FOUND,
          "Attempt " + attempt + " of stage '" + stageKey + "' of iteration '" + iterationId + "'");
    }
    var trace = traceRepository.findByIterationIdAndStageKeyAndAttempt(iterationId, stageKey, attempt);
    var events = eventRepository
        .findByIterationIdAndStageKeyAndAttemptOrderBySeqAsc(iterationId, stageKey, attempt).stream()
        .map(PipelineTraceServiceImpl::toEventRS)
        .toList();
    return PipelineTraceRS.builder()
        .iterationId(iterationId)
        .stageKey(stageKey)
        .attempt(attempt)
        .status(trace.map(PipelineStageTrace::getStatus).orElse(TraceStatus.NOT_REPORTED))
        .reason(trace.map(PipelineStageTrace::getNotReportedReason).orElse(null))
        .reportedAt(trace.map(PipelineStageTrace::getReportedAt).orElse(null))
        .events(events)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PipelineTraceAttemptRS> getTraceAttempts(Long projectId, Long iterationId, String stageKey) {
    var stage = findStageOfProject(projectId, iterationId, stageKey);
    var traces = traceRepository.findByIterationIdAndStageKeyOrderByAttemptAsc(iterationId, stageKey).stream()
        .collect(Collectors.toMap(PipelineStageTrace::getAttempt, Function.identity()));
    var counts = eventRepository.countByAttempt(iterationId, stageKey).stream()
        .collect(Collectors.toMap(c -> c.getAttempt(), c -> c.getCount()));

    var result = new ArrayList<PipelineTraceAttemptRS>();
    for (var finished : pipelineStageAttemptRepository.findByIterationIdAndStageKeyOrderByAttemptAsc(iterationId,
        stageKey)) {
      result.add(attemptRS(finished.getAttempt(), finished.getStatus(), finished.getStatusCategory(), false, traces,
          counts));
    }
    result.add(attemptRS(stage.getAttempt(), stage.getStatus(), stage.getStatusCategory(), true, traces, counts));
    return result;
  }

  private static PipelineTraceAttemptRS attemptRS(int attempt, String status,
      com.epam.reportportal.base.core.aifactory.enums.StatusCategory category, boolean current,
      Map<Integer, PipelineStageTrace> traces, Map<Integer, Long> counts) {
    var trace = traces.get(attempt);
    return PipelineTraceAttemptRS.builder()
        .attempt(attempt)
        .stageStatus(status)
        .stageStatusCategory(category)
        .traceStatus(trace == null ? TraceStatus.NOT_REPORTED : trace.getStatus())
        .eventCount(counts.getOrDefault(attempt, 0L))
        .reportedAt(trace == null ? null : trace.getReportedAt())
        .current(current)
        .build();
  }

  private static PipelineTraceRS.Event toEventRS(PipelineStageTraceEvent event) {
    return PipelineTraceRS.Event.builder()
        .seq(event.getSeq())
        .type(event.getType())
        .title(event.getTitle())
        .offsetMs(event.getOffsetMs())
        .durationMs(event.getDurationMs())
        .target(event.getTarget())
        .text(event.getText())
        .data(event.getData() == null ? null : event.getData().getContent())
        .build();
  }

  private PipelineStage findStageOfProject(Long projectId, Long iterationId, String stageKey) {
    var iteration = pipelineIterationRepository.findById(iterationId)
        .filter(it -> it.getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline iteration '" + iterationId + "'"));
    return findStage(iteration.getId(), stageKey);
  }

  private PipelineStage findStage(Long iterationId, String stageKey) {
    return pipelineStageRepository.findByIterationIdAndStageKey(iterationId, stageKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Stage '" + stageKey + "' of iteration '" + iterationId + "'"));
  }
}
