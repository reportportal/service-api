package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.TraceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tells whether the trace of one attempt of a stage was reported. Its events are
 * {@link PipelineStageTraceEvent}s with the same {@code (iterationId, stageKey, attempt)}.
 */
@Entity
@Table(name = "pipeline_stage_trace", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class PipelineStageTrace implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "iteration_id", nullable = false)
  private Long iterationId;

  @Column(name = "stage_key", nullable = false)
  private String stageKey;

  @Column(name = "attempt", nullable = false)
  private int attempt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TraceStatus status;

  @Column(name = "not_reported_reason", columnDefinition = "text")
  private String notReportedReason;

  @Column(name = "reported_at", nullable = false)
  private Instant reportedAt;
}
