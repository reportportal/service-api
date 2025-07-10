package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tms_test_case_version", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseVersion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "is_default")
  private boolean isDefault;

  @Column(name = "is_draft")
  private boolean isDraft;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "test_case_id")
  private TmsTestCase testCase;

  @OneToOne(mappedBy = "testCaseVersion", fetch = FetchType.LAZY)
  private TmsManualScenario manualScenario;

  //TODO: override equals and hashCode methods

}
