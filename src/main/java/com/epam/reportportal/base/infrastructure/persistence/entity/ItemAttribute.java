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

package com.epam.reportportal.base.infrastructure.persistence.entity;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
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
import lombok.Data;

/**
 * Name/value (and system flag) attribute attached to a test item.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Data
@Entity
@Table(name = "item_attribute")
public class ItemAttribute implements Attribute, Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "key")
  private String key;

  @Column(name = "value")
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private TestItem testItem;

  @Column(name = "system")
  private Boolean system;

  public ItemAttribute() {
  }

  public ItemAttribute(String key, String value, Boolean system) {
    this.key = key;
    this.value = value;
    this.system = system;
  }

  public Long getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public TestItem getTestItem() {
    return testItem;
  }

  public Boolean isSystem() {
    return system;
  }

  /*
   *	DO NOT REGENERATE EQUALS AND HASHCODE!
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemAttribute that = (ItemAttribute) o;

    return Objects.equals(key, that.key) && Objects.equals(value, that.value)
        && Objects.equals(system, that.system) && Objects.equals(
        testItem != null ? "testItem:" + (testItem.getItemId() != null ? testItem.getItemId() : "")
            : "testItem:",
        that.testItem != null ? "testItem:" + (that.testItem.getItemId() != null
            ? that.testItem.getItemId() : "") : "testItem:"
    );
  }

  /*
   *	DO NOT REGENERATE EQUALS AND HASHCODE!
   */
  @Override
  public int hashCode() {
    return Objects.hash(key,
        value,
        system,
        testItem != null ? "testItem:" + (testItem.getItemId() != null ? testItem.getItemId() : "")
            : "testItem:"
    );
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ItemAttribute{");
    sb.append("id=").append(id);
    sb.append(", key='").append(key).append('\'');
    sb.append(", value='").append(value).append('\'');
    sb.append(", system=").append(system);
    sb.append(testItem != null ? ", testItem=" + testItem.getItemId() : "");
    sb.append('}');
    return sb.toString();
  }
}
