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

package com.epam.reportportal.base.infrastructure.persistence.commons.accessible;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Representation of accessible method or field
 *
 * @author Andrei Varabyeu
 */
public class Accessible {

  private final Object object;

  /**
   * Binds a helper to the given instance for method and field access.
   *
   * @param object target to reflect on
   */
  private Accessible(Object object) {
    this.object = object;

  }

  /**
   * Wraps the object for subsequent {@link #method(java.lang.reflect.Method)} or
   * {@link #field(java.lang.reflect.Field)} access.
   *
   * @param object object whose members will be wrapped
   * @return reflect helper for the instance
   */
  public static Accessible on(Object object) {
    return new Accessible(object);
  }

  /**
   * Exposes a method for reflective invocation, lifting accessibility as needed.
   *
   * @param m method to wrap for invocation
   * @return method accessor bound to the same instance
   */
  public AccessibleMethod method(Method m) {
    return new AccessibleMethod(object, m);
  }

  /**
   * Exposes a field for get/set, lifting accessibility as needed.
   *
   * @param f field to wrap for get/set
   * @return field accessor bound to the same instance
   */
  public AccessibleField field(Field f) {
    return new AccessibleField(object, f);
  }
}
