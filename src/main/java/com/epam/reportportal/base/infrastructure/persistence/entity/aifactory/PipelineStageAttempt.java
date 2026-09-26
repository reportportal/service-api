package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

/**
 * A finished attempt of a stage, kept when the stage is run again (a retry, or a stage reported anew).
 * Keyed by {@code (iterationId, stageKey)} rather than the stage id so it survives the stage row being
 * recreated.
 */
@Entity
@Table(name = "pipeline_stage_attempt", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageAttempt implements Serializable {

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

  @Column(name = "status", nullable = false)
  private String status;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_category", nullable = false)
  private StatusCategory statusCategory;

  @Column(name = "started_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant startedAt;

  @Column(name = "finished_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant finishedAt;

  @Type(PipelineExecutionData.class)
  @Column(name = "execution", columnDefinition = "jsonb")
  private PipelineExecutionData execution;

  @Column(name = "result_type")
  private String resultType;

  @Column(name = "result_ref")
  private String resultRef;

  @Type(PipelineAttributes.class)
  @Column(name = "attributes", columnDefinition = "jsonb")
  private PipelineAttributes attributes;

  @Type(PipelineSections.class)
  @Column(name = "sections", columnDefinition = "jsonb")
  private PipelineSections sections;

  /**
   * Snapshot of the stage's current attempt, taken just before the stage starts a new one.
   */
  public static PipelineStageAttempt snapshotOf(PipelineStage stage) {
    var attempt = new PipelineStageAttempt();
    attempt.setIterationId(stage.getIteration().getId());
    attempt.setStageKey(stage.getStageKey());
    attempt.setAttempt(stage.getAttempt());
    attempt.setStatus(stage.getStatus());
    attempt.setStatusCategory(stage.getStatusCategory());
    attempt.setStartedAt(stage.getStartedAt());
    attempt.setFinishedAt(stage.getFinishedAt());
    attempt.setExecution(stage.getExecution());
    attempt.setResultType(stage.getResultType());
    attempt.setResultRef(stage.getResultRef());
    attempt.setAttributes(stage.getAttributes() == null ? null : new PipelineAttributes(stage.getAttributes().getItems()));
    attempt.setSections(stage.getSections() == null ? null : new PipelineSections(stage.getSections().getItems()));
    return attempt;
  }
}
