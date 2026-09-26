package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

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
 * A single scored criterion of a {@link TmsQualityStandard} (e.g. "Scenario
 * correctness", worth 25 of the standard's 100 points).
 */
@Entity
@Table(name = "tms_quality_standard_criterion", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsQualityStandardCriterion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "standard_id", nullable = false)
  private TmsQualityStandard standard;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "max_points", nullable = false)
  private Integer maxPoints;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;
}
