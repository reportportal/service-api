package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * How a {@link TmsTestCaseVersion} was generated: tokens, model, skill and
 * cost. 1:1 with the version it describes, following the same "belongs to the
 * version" reasoning as {@link TmsTestCaseQualityScore}.
 */
@Entity
@Table(name = "tms_test_case_generation_metadata", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseGenerationMetadata implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_case_version_id", nullable = false, unique = true)
  private TmsTestCaseVersion testCaseVersion;

  @Column(name = "tokens_in")
  private Integer tokensIn;

  @Column(name = "tokens_out")
  private Integer tokensOut;

  @Column(name = "model")
  private String model;

  @Column(name = "skill")
  private String skill;

  @Column(name = "cost_usd")
  private BigDecimal costUsd;
}
