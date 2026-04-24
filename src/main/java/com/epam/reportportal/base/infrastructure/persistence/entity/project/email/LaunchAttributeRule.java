/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.entity.project.email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * When to run a {@link SenderCase} (match on launch attribute).
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Entity
@Table(name = "launch_attribute_rules")
public class LaunchAttributeRule implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "key")
  private String key;

  @Column(name = "value")
  private String value;

  @ManyToOne
  @JoinColumn(name = "sender_case_id")
  private SenderCase senderCase;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public SenderCase getSenderCase() {
    return senderCase;
  }

  public void setSenderCase(SenderCase senderCase) {
    this.senderCase = senderCase;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LaunchAttributeRule that = (LaunchAttributeRule) o;
    return Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(value,
        that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, key, value);
  }
}
