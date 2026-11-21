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

package com.epam.reportportal.base.ws.resolver;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdScalarSerializer;

/**
 * Module for custom serializer configuration for {@link JacksonViewAware}
 *
 * @author Andrei Varabyeu
 */
public class JacksonViewAwareModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  /**
   * @param objectMapper - We need to provide JsonMapper here since it's impossible to serialize using JSON Views
   *                     without mapper. It's little bit unusual from Jackson point of view, but this is only one way to
   *                     avoid 'instaceOf' and classcast on http message converters level
   * @see <a href="http://wiki.fasterxml.com/JacksonHowToCustomSerializers">Jackson - HowTo -
   * CustomSerializers</a>
   */
  public JacksonViewAwareModule(JsonMapper objectMapper) {
    addSerializer(JacksonViewAware.class, new JacksonViewAwareSerializer(objectMapper));
  }

  public static class JacksonViewAwareSerializer extends StdScalarSerializer<JacksonViewAware> {

    private final JsonMapper objectMapper;

    protected JacksonViewAwareSerializer(JsonMapper objectMapper) {
      super(JacksonViewAware.class);
      this.objectMapper = objectMapper;
    }

    @Override
    public void serialize(JacksonViewAware value, JsonGenerator jgen, SerializationContext provider) {
      objectMapper.writerWithView(value.getView()).writeValue(jgen, value.getPojo());
    }

  }

}
