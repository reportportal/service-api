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

package com.epam.reportportal.base.core.configs;

import com.epam.reportportal.base.ws.resolver.JacksonViewAwareModule;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * JsonMapper and Jackson modules for API and events.
 *
 * @author Andrei Varabyeu
 */
@Configuration
public class JacksonConfiguration {

  /**
   * Disables {@link MapperFeature#DETECT_PARAMETER_NAMES} so Jackson 3 keeps field defaults of OpenAPI models (e.g.
   * {@code SearchCriteriaRQ.limit = 300}) instead of overriding them with {@code null} via the all-args constructor.
   *
   * @return Configured object mapper
   */
  @Bean(name = "objectMapper")
  public JsonMapper objectMapper() {
    JsonMapper jsonMapper = JsonMapper.builder()
        .disable(MapperFeature.DETECT_PARAMETER_NAMES)
        .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_NULL))
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();

    return jsonMapper.rebuild()
        .addModule(new JacksonViewAwareModule(jsonMapper))
        .build();
  }
}
