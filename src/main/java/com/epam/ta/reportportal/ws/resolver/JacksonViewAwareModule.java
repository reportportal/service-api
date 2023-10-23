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

package com.epam.ta.reportportal.ws.resolver;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;

/**
 * Module for custom serializer configuration for {@link JacksonViewAware}
 *
 * @author Andrei Varabyeu
 */
public class JacksonViewAwareModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  /**
   * @param objectMapper - We need to provide ObjectMapper here since it's impossible to serialize
   *                     using JSON Views without mapper. It's little bit unusual from Jackson point
   *                     of view, but this is only one way to avoid 'instaceOf' and classcast on
   *                     http message converters level
   * @see <a href="http://wiki.fasterxml.com/JacksonHowToCustomSerializers">Jackson - HowTo -
   * CustomSerializers</a>
   */
  public JacksonViewAwareModule(ObjectMapper objectMapper) {
    addSerializer(JacksonViewAware.class, new JacksonViewAwareSerializer(objectMapper));
  }

  public static class JacksonViewAwareSerializer extends StdScalarSerializer<JacksonViewAware> {

    private ObjectMapper objectMapper;

    protected JacksonViewAwareSerializer(ObjectMapper objectMapper) {
      super(JacksonViewAware.class);
      this.objectMapper = objectMapper;
    }

    @Override
    public void serialize(JacksonViewAware value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {
      /*
       * Writes bean with specified view
       */
      objectMapper.writerWithView(value.getView()).writeValue(jgen, value.getPojo());
    }

  }

}