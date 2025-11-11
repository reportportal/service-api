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
import java.lang.reflect.Method;

/**
 * Representation of accessible method or field
 *
 * @author Andrei Varabyeu
 */
public class Accessible {

  private final Object object;

  private Accessible(Object object) {
    this.object = object;

  }

  public static Accessible on(Object object) {
    return new Accessible(object);
  }

  public AccessibleMethod method(Method m) {
    return new AccessibleMethod(object, m);
  }

  public AccessibleField field(Field f) {
    return new AccessibleField(object, f);
  }
}
