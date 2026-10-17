package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import com.epam.reportportal.base.infrastructure.persistence.entity.LTreeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A single step ("any activity") within a {@link PipelineIteration}, close to an
 * OpenTelemetry span / Temporal activity: free-form {@code stageKey}, and may
 * nest other stages via {@code parentStageId}/{@code path} (same convention as
 * {@code TestItem.path} — maintained in application code, no DB trigger), so
 * any business process can be modelled, not just test-case generation. Carries
 * its own CI reference so a stage can be individually retried by re-triggering
 * the exact job that ran it, and a generic {@code resultType}/{@code resultRef}
 * pointer for whatever artifact it produced (e.g. a Launch).
 * <p>
 * {@code status} is the text exactly as reported; {@code statusCategory} is what it counts as.
 * What the stage did is described by typed {@code attributes} (the chip metric, cost, ...) and
 * {@code sections} (result tables); {@code rawOutput} keeps what the stage reported as received.
 */
@Entity
@Table(name = "pipeline_stage", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStage implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "iteration_id", nullable = false)
  private PipelineIteration iteration;

  @Column(name = "stage_key", nullable = false)
  private String stageKey;

  @Column(name = "name")
  private String name;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Column(name = "attempt", nullable = false)
  private int attempt = 1;

  @Column(name = "parent_stage_id")
  private Long parentStageId;

  @Column(name = "path", columnDefinition = "ltree")
  @Type(LTreeType.class)
  private String path;

  @Column(name = "status", nullable = false)
  private String status;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_category", nullable = false)
  private StatusCategory statusCategory;

  @Enumerated(EnumType.STRING)
  @Column(name = "owned_by", nullable = false)
  private StageOwner ownedBy = StageOwner.PIPELINE;

  @Column(name = "affects_iteration_status", nullable = false)
  private boolean affectsIterationStatus = true;

  @Type(PipelineAttributes.class)
  @Column(name = "attributes", columnDefinition = "jsonb")
  private PipelineAttributes attributes;

  @Type(PipelineSections.class)
  @Column(name = "sections", columnDefinition = "jsonb")
  private PipelineSections sections;

  @Type(PipelineRelatedEntities.class)
  @Column(name = "related_entities", columnDefinition = "jsonb")
  private PipelineRelatedEntities relatedEntities;

  @Column(name = "summary")
  private String summary;

  @Column(name = "message")
  private String message;

  @Type(PipelineLinks.class)
  @Column(name = "links", columnDefinition = "jsonb")
  private PipelineLinks links;

  @Type(PipelineRawOutput.class)
  @Column(name = "raw_output", columnDefinition = "jsonb")
  private PipelineRawOutput rawOutput;

  @Column(name = "raw_output_schema_name")
  private String rawOutputSchemaName;

  @Column(name = "result_type")
  private String resultType;

  @Column(name = "result_ref")
  private String resultRef;

  @Column(name = "started_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant startedAt;

  @Column(name = "finished_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant finishedAt;

  @Type(PipelineExecutionData.class)
  @Column(name = "execution", columnDefinition = "jsonb")
  private PipelineExecutionData execution;

  @Column(name = "last_retried_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant lastRetriedAt;

  @Column(name = "last_retried_by")
  private Long lastRetriedBy;
}
