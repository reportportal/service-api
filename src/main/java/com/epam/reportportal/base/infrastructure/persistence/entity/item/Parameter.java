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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Key/value parameter for a data-driven or parameterized test, embedded in {@link TestItem}.
 *
 * @author Pavel Bortnik
 */
@Embeddable
public class Parameter implements Serializable {

  @Column(name = "key")
  private String key;

  @Column(name = "value")
  private String value;

  public Parameter() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Parameter parameter = (Parameter) o;

    if (key != null ? !key.equals(parameter.key) : parameter.key != null) {
      return false;
    }
    return value != null ? value.equals(parameter.value) : parameter.value == null;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }
}
