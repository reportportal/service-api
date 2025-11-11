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

package com.epam.reportportal.infrastructure.persistence.commons.accessible;

import java.lang.reflect.Field;

/**
 * Setter and Getter for Accessible Field
 *
 * @author Andrei Varabyeu
 */
public class AccessibleField {

  private final Field f;
  private final Object bean;

  AccessibleField(Object bean, Field f) {
    this.bean = bean;
    this.f = f;
  }

  public Class<?> getType() {
    return this.f.getType();
  }

  public Object getValue() {
    try {
      return this.f.get(this.bean);
    } catch (IllegalAccessException accessException) { //NOSONAR
      this.f.setAccessible(true);
      try {
        return this.f.get(this.bean);
      } catch (IllegalAccessException e) { //NOSONAR
        throw new IllegalAccessError(e.getMessage());
      }
    }
  }

  public void setValue(Object value) {
    try {
      this.f.set(this.bean, value);
    } catch (IllegalAccessException accessException) { //NOSONAR
      this.f.setAccessible(true);
      try {
        this.f.set(this.bean, value);
      } catch (IllegalAccessException e) { //NOSONAR
        throw new IllegalAccessError(e.getMessage());
      }
    }
  }
}
