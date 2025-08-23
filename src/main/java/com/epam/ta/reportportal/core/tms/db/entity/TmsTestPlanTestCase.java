package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tms_test_plan_test_case")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlanTestCase {

  @EmbeddedId
  private TmsTestPlanTestCaseId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("testPlanId")
  @JoinColumn(name = "test_plan_id", nullable = false)
  private TmsTestPlan testPlan;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("testCaseId")
  @JoinColumn(name = "test_case_id", nullable = false)
  private TmsTestCase testCase;

  /**
   * Convenience constructor for creating association
   */
  public TmsTestPlanTestCase(TmsTestPlan testPlan, TmsTestCase testCase) {
    this.testPlan = testPlan;
    this.testCase = testCase;
    this.id = new TmsTestPlanTestCaseId(testPlan.getId(), testCase.getId());
  }

  /**
   * Convenience constructor with IDs only
   */
  public TmsTestPlanTestCase(Long testPlanId, Long testCaseId) {
    this.id = new TmsTestPlanTestCaseId(testPlanId, testCaseId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TmsTestPlanTestCase that = (TmsTestPlanTestCase) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "TmsTestPlanTestCase{" +
        "testPlanId=" + (id != null ? id.getTestPlanId() : null) +
        ", testCaseId=" + (id != null ? id.getTestCaseId() : null) +
        '}';
  }
}
