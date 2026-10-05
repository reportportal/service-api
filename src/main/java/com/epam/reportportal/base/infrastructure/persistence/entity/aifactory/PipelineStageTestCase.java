package com.epam.reportportal.base.infrastructure.persistence.entity.aifactory;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Links a {@link PipelineStage} to a {@link TmsTestCase} it generated or touched.
 * Unidirectional on purpose: {@code TmsTestCase} carries no knowledge of Pipeline
 * at all, so the TMS entity is untouched by this feature.
 */
@Entity
@Table(name = "pipeline_stage_test_case", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStageTestCase implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stage_id", nullable = false)
  private PipelineStage stage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_case_id", nullable = false)
  private TmsTestCase testCase;
}
