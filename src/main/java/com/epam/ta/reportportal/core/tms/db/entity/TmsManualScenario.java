package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tms_manual_scenario", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmsManualScenario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "execution_estimation_time")
  private Integer executionEstimationTime;

  @Column(name = "link_to_requirements")
  private String linkToRequirements;

  @Column(name = "preconditions")
  private String preconditions;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_case_version_id")
  private TmsTestCaseVersion testCaseVersion;

  @OneToMany(mappedBy = "manualScenario")
  private Set<TmsManualScenarioAttribute> attributes;

  @OneToMany(mappedBy = "manualScenario")
  private Set<TmsStep> steps;
}
