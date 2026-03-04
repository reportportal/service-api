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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Accessible method implementation. Set accessibility == true for specified method and can invoke methods
 *
 * @author Andrei Varabyeu
 */
public class AccessibleMethod {

  private final Method method;
  private final Object bean;

  AccessibleMethod(Object bean, Method method) {
    this.bean = bean;
    this.method = method;
  }

  public Object invoke(Object... args) throws Throwable {
    try {
      return invoke(this.bean, this.method, args);
    } catch (IllegalAccessException accessException) { //NOSONAR
      this.method.setAccessible(true);
      try {
        return invoke(this.bean, this.method, args);
      } catch (IllegalAccessException e) { //NOSONAR
        throw new IllegalAccessError(e.getMessage());
      }
    }

  }

  private Object invoke(Object bean, Method m, Object... args) throws Throwable {
    try {
      return m.invoke(bean, args);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }

  }

}
