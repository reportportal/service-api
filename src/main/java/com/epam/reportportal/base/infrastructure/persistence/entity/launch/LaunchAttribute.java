/*
 * Copyright 2026 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.entity.launch;

import com.epam.reportportal.base.infrastructure.persistence.entity.Attribute;
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
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Name/value (and system flag) attribute attached to a launch.
 */
@Getter
@Setter
@Entity
@Table(name = "launch_attribute")
public class LaunchAttribute implements Attribute, Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "key")
  private String key;

  @Column(name = "value")
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "launch_id")
  private Launch launch;

  @Column(name = "system")
  private Boolean system;

  public LaunchAttribute() {
  }

  public LaunchAttribute(String key, String value, Boolean system) {
    this.key = key;
    this.value = value;
    this.system = system;
  }

  public Boolean isSystem() {
    return system;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LaunchAttribute that = (LaunchAttribute) o;

    return Objects.equals(key, that.key) && Objects.equals(value, that.value)
        && Objects.equals(system, that.system) && Objects.equals(
        launch != null ? "launch:" + (launch.getId() != null ? launch.getId() : "")
            : "launch:",
        that.launch != null ? "launch:" + (that.launch.getId() != null
            ? that.launch.getId() : "") : "launch:"
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(key,
        value,
        system,
        launch != null ? "launch:" + (launch.getId() != null ? launch.getId() : "")
            : "launch:"
    );
  }
}
