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

package com.epam.reportportal.base.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorRS;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationFeature;

/**
 * Serializer/Deserializer unit tests
 *
 * @author Andrei Varabyeu
 */
public class ErrorSerializationTest {

  private static final String SERIALIZATION_RESULT = "{\"errorCode\":5000,\"message\":\"MESSAGE\",\"stackTrace\":\"STACK_TRACE\"}";

  private final JsonMapper om = getObjectMapper();

  @Test
  public void testSerializer() {
    String json = om.writeValueAsString(getErrorResponse());
    assertEquals(SERIALIZATION_RESULT, json);
  }

  @Test
  public void testDeserializer() {
    ErrorRS fromJson = om.readValue(SERIALIZATION_RESULT, ErrorRS.class);
    assertEquals(getErrorResponse(), fromJson);
  }

  private ErrorRS getErrorResponse() {
    ErrorRS error = new ErrorRS();
    error.setMessage("MESSAGE");
    error.setStackTrace("STACK_TRACE");
    error.setErrorType(ErrorType.UNCLASSIFIED_ERROR);
    return error;
  }

  private JsonMapper getObjectMapper() {
    return JsonMapper.builder()
        .disable(SerializationFeature.INDENT_OUTPUT)
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();
  }
}
