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

package com.epam.reportportal.base.util;

import com.epam.reportportal.base.ws.resolver.JacksonViewAwareModule;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Single {@link JsonMapper} shared by all test infrastructure. Mirrors the settings of the production
 * {@code JacksonConfiguration} so that test serialization matches the real API output.
 */
public final class TestJsonMapper {

  public static final JsonMapper INSTANCE = create();

  private TestJsonMapper() {
  }

  private static JsonMapper create() {
    JsonMapper base = JsonMapper.builder()
        .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_NULL))
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();

    return base.rebuild()
        .addModule(new JacksonViewAwareModule(base))
        .build();
  }
}
