package com.epam.reportportal.infrastructure.persistence.entity.tms;

import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing TMS Manual Launch Attribute.
 */
@Entity
@Table(name = "tms_manual_launch_attribute")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmsManualLaunchAttribute implements Serializable {

  @EmbeddedId
  private TmsManualLaunchAttributeId id;

  @ManyToOne
  @MapsId(value = "launchId")
  @JoinColumn(name = "launch_id")
  private Launch launch;

  @ManyToOne
  @MapsId(value = "attributeId")
  @JoinColumn(name = "attribute_id")
  private TmsAttribute attribute;

  @Column(name = "value")
  private String value;
}
