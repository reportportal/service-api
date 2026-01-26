package com.epam.reportportal.infrastructure.persistence.entity.tms;

import com.epam.reportportal.infrastructure.persistence.entity.ItemAttribute;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Junction entity linking TmsTestPlan with ItemAttribute.
 * This entity can be extended with additional fields in the future
 * (e.g., created_at, order, metadata).
 */
@Entity
@Table(name = "tms_test_plan_attribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TmsTestPlanAttribute implements Serializable {

  @EmbeddedId
  private TmsTestPlanAttributeId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId(value = "testPlanId")
  @JoinColumn(name = "test_plan_id")
  private TmsTestPlan testPlan;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId(value = "attributeId")
  @JoinColumn(name = "attribute_id")
  private ItemAttribute itemAttribute;

  /**
   * Convenience constructor for creating junction entity.
   *
   * @param testPlan the test plan
   * @param itemAttribute the item attribute
   */
  public TmsTestPlanAttribute(TmsTestPlan testPlan, ItemAttribute itemAttribute) {
    this.testPlan = testPlan;
    this.itemAttribute = itemAttribute;
    this.id = new TmsTestPlanAttributeId(
        testPlan != null ? testPlan.getId() : null,
        itemAttribute != null ? itemAttribute.getId() : null
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var that = (TmsTestPlanAttribute) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
