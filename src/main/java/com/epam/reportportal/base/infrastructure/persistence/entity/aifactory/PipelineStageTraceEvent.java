package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.TraceEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * One event of the trace of a stage attempt. The text is kept exactly as reported, never cut.
 */
@Entity
@Table(name = "pipeline_stage_trace_event", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class PipelineStageTraceEvent implements Serializable {

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

  @Column(name = "seq", nullable = false)
  private int seq;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private TraceEventType type;

  @Column(name = "title")
  private String title;

  @Column(name = "offset_ms")
  private Long offsetMs;

  @Column(name = "duration_ms")
  private Long durationMs;

  /** Key of the units-section row that produced the event, e.g. "test-case-review-single#2#1"; null for the stage itself. */
  @Column(name = "unit_key")
  private String unitKey;

  @Column(name = "target")
  private String target;

  @Column(name = "text", columnDefinition = "text")
  private String text;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "data", columnDefinition = "jsonb")
  private PipelineRawOutput data;
}
