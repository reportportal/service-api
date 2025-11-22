package com.epam.reportportal.infrastructure.persistence.entity.tms;

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
public class TmsManualLaunchAttributeId implements Serializable {

  @Column(name = "launch_id")
  private Long launchId;

  @Column(name = "attribute_id")
  private Long attributeId;

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TmsManualLaunchAttributeId that = (TmsManualLaunchAttributeId) o;
    return Objects.equals(launchId, that.launchId) && Objects.equals(attributeId,
        that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(launchId, attributeId);
  }
}
