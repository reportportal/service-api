/*
 * Copyright 2020 EPAM Systems
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

package com.epam.reportportal.base.extension.classloader;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo;

import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DelegatingClassLoader extends ClassLoader {

  private static final String APPLICATION_CLASS_LOADER = "applicationClassLoader";

  private final Map<String, ClassLoader> delegates;

  public DelegatingClassLoader() {
    this.delegates = new LinkedHashMap<>();
    this.delegates.put(APPLICATION_CLASS_LOADER, this.getClass().getClassLoader());
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    for (ClassLoader classLoader : delegates.values()) {
      try {
        return classLoader.loadClass(name);
      } catch (ClassNotFoundException e) {
        //let another class loader try
      }
    }
    throw new ClassNotFoundException(name);
  }

  public void addLoader(String key, ClassLoader classLoader) {
    BusinessRule.expect(key.equals(APPLICATION_CLASS_LOADER), equalTo(false))
        .verify(ErrorType.PLUGIN_UPLOAD_ERROR, APPLICATION_CLASS_LOADER + " key is reserved");
    delegates.put(key, classLoader);
  }

  public void removeLoader(String key) {
    BusinessRule.expect(key.equals(APPLICATION_CLASS_LOADER), equalTo(false))
        .verify(ErrorType.PLUGIN_UPLOAD_ERROR, APPLICATION_CLASS_LOADER + " key is reserved");
    delegates.remove(key);
  }
}
