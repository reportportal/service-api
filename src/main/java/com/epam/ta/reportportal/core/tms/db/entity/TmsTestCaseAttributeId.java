package com.epam.ta.reportportal.core.tms.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestCaseAttributeId implements Serializable {

  @Column(name = "test_case_id")
  private Long testCaseId;

  @Column(name = "attribute_id")
  private Long attributeId;

  //TODO: override equals and hashCode methods


  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TmsTestCaseAttributeId that = (TmsTestCaseAttributeId) o;
    return Objects.equals(testCaseId, that.testCaseId) && Objects.equals(attributeId,
        that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(testCaseId, attributeId);
  }
}
