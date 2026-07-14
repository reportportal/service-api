/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.commons;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Gives {@link JsonbUserType} access to the Spring-managed {@code objectMapper} bean.
 *
 * <p>Hibernate instantiates {@link JsonbUserType} subclasses via reflection, not through the
 * Spring bean container, so the mapper can't be constructor/field injected there directly. This
 * holder captures the {@link ApplicationContext} once at startup and resolves the bean lazily on
 * first use, by which point the context is fully initialized.
 */
@Component
public class JsonbObjectMapperHolder implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    JsonbObjectMapperHolder.applicationContext = applicationContext;
  }

  static ObjectMapper getObjectMapper() {
    return applicationContext.getBean("objectMapper", ObjectMapper.class);
  }
}
