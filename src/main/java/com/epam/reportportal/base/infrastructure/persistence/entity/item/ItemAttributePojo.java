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

package com.epam.reportportal.base.infrastructure.persistence.entity.item;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class ItemAttributePojo implements Serializable {

  private Long itemId;

  private String key;

  private String value;

  private boolean isSystem;

  public ItemAttributePojo() {
  }

  public ItemAttributePojo(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public ItemAttributePojo(Long itemId, String key, String value, boolean isSystem) {
    this.itemId = itemId;
    this.key = key;
    this.value = value;
    this.isSystem = isSystem;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
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

  public boolean isSystem() {
    return isSystem;
  }

  public void setSystem(boolean system) {
    isSystem = system;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemAttributePojo that = (ItemAttributePojo) o;
    return isSystem == that.isSystem && Objects.equals(itemId, that.itemId) && Objects.equals(key,
        that.key) && Objects.equals(
        value,
        that.value
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemId, key, value, isSystem);
  }
}
