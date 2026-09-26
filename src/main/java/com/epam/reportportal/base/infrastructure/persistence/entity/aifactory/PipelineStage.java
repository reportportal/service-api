package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
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
 * A single stage ("any activity") within a {@link PipelineIteration}. Free-form
 * {@code stageKey}/{@code agent} rather than a fixed enum, since the set of
 * possible activities is open-ended. Carries its own CI reference so a stage can
 * be individually retried by re-triggering the exact job that ran it.
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

  @Column(name = "short_name")
  private String shortName;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @Column(name = "agent")
  private String agent;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PipelineRunStatus status;

  @Type(PipelineMetrics.class)
  @Column(name = "metrics", columnDefinition = "jsonb")
  private PipelineMetrics metrics;

  @Type(PipelineGraders.class)
  @Column(name = "graders", columnDefinition = "jsonb")
  private PipelineGraders graders;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "ci_provider")
  private CiProvider ciProvider;

  @Column(name = "ci_repo")
  private String ciRepo;

  @Column(name = "ci_workflow_ref")
  private String ciWorkflowRef;

  @Column(name = "ci_run_id")
  private String ciRunId;

  @Column(name = "ci_job_id")
  private String ciJobId;

  @Column(name = "ci_run_url")
  private String ciRunUrl;

  @Column(name = "retryable", nullable = false)
  private boolean retryable;

  @Column(name = "last_retried_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant lastRetriedAt;

  @Column(name = "last_retried_by")
  private Long lastRetriedBy;
}
