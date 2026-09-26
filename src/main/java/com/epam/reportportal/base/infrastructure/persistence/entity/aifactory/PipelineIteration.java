package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import jakarta.persistence.Column;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * One run of a {@link Pipeline}: a numbered iteration made up of stages, ingested
 * from CI. No direct link to {@code Launch} — the real Pipeline-to-Launch
 * relationship, if any test case it generates is later executed, already exists
 * through {@code TmsTestCase -> TmsTestCaseExecution -> TestItem -> Launch}.
 * <p>
 * Everything descriptive (requirement, trigger, model, cost, tags, links) lives in the typed
 * {@code attributes}; {@code searchText} is derived from them on write for list search.
 * {@code status} is the derived label of the iteration, {@code statusCategory} what it counts as.
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

  @Column(name = "status", nullable = false)
  private String status;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_category", nullable = false)
  private StatusCategory statusCategory;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "attributes", columnDefinition = "jsonb")
  private PipelineAttributes attributes;

  @Column(name = "summary")
  private String summary;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "details", columnDefinition = "TEXT")
  private String details;

  @Column(name = "search_text", columnDefinition = "TEXT")
  private String searchText;

  @Column(name = "attribute_index", columnDefinition = "TEXT")
  private String attributeIndex;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "rerun", nullable = false)
  private boolean rerun;

  @Column(name = "idempotency_key")
  private String idempotencyKey;

  @Column(name = "idempotency_hash")
  private String idempotencyHash;

  @Column(name = "rerun_of_iteration_id")
  private Long rerunOfIterationId;

  @Column(name = "created_by")
  private Long createdBy;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
