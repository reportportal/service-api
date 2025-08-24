package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlanTestCaseId implements Serializable {

  @Column(name = "test_plan_id", nullable = false)
  private Long testPlanId;

  @Column(name = "test_case_id", nullable = false)
  private Long testCaseId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TmsTestPlanTestCaseId that = (TmsTestPlanTestCaseId) o;
    return Objects.equals(testPlanId, that.testPlanId) &&
        Objects.equals(testCaseId, that.testCaseId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(testPlanId, testCaseId);
  }
}
