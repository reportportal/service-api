package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.converters.JpaInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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

/**
 * The AI-agent-reported score of one {@link TmsQualityStandardCriterion} for a
 * specific {@link TmsTestCaseVersion}. The version this belongs to earned the
 * score for its content, so a new version naturally makes the score obsolete.
 */
@Entity
@Table(name = "tms_test_case_quality_score", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseQualityScore implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_case_version_id", nullable = false)
  private TmsTestCaseVersion testCaseVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "criterion_id", nullable = false)
  private TmsQualityStandardCriterion criterion;

  @Column(name = "score", nullable = false)
  private Integer score;

  @Column(name = "evaluated_at", nullable = false)
  @Convert(converter = JpaInstantConverter.class)
  private Instant evaluatedAt;

  /**
   * Optional link to the Pipeline iteration that produced this score. Plain id,
   * not a JPA relation: the Pipeline domain is independent of TMS, and this is
   * the only place TMS points back at it.
   */
  @Column(name = "pipeline_iteration_id")
  private Long pipelineIterationId;
}
