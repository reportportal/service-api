package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * One run of a {@link Pipeline}: a numbered iteration made up of stages, ingested
 * from CI. No direct link to {@code Launch} — the real Pipeline-to-Launch
 * relationship, if any test case it generates is later executed, already exists
 * through {@code TmsTestCase -> TmsTestCaseExecution -> TestItem -> Launch}.
 */
@Entity
@Table(name = "pipeline_iteration", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineIteration implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pipeline_id", nullable = false)
  private Pipeline pipeline;

  @Column(name = "iteration_number", nullable = false)
  private Integer iterationNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PipelineRunStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "quality_gate")
  private PipelineRunStatus qualityGate;

  @Type(PipelineMetrics.class)
  @Column(name = "metrics", columnDefinition = "jsonb")
  private PipelineMetrics metrics;

  @Column(name = "trigger")
  private String trigger;

  @Column(name = "started_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant startedAt;

  @Column(name = "finished_at")
  @Convert(converter = JpaInstantConverter.class)
  private Instant finishedAt;

  @Column(name = "rerun", nullable = false)
  private boolean rerun;

  @Column(name = "rerun_of_iteration_id")
  private Long rerunOfIterationId;

  @Column(name = "created_by")
  private Long createdBy;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant createdAt;
}
