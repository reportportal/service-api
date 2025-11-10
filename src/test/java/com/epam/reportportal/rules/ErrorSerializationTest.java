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

package com.epam.reportportal.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.infrastructure.rules.exception.ErrorRS;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Serializer/Deserializer unit tests
 *
 * @author Andrei Varabyeu
 */
public class ErrorSerializationTest {

  private static final String SERIALIZATION_RESULT = "{\"errorCode\":5000,\"message\":\"MESSAGE\",\"stackTrace\":\"STACK_TRACE\"}";

  private final ObjectMapper om = getObjectMapper();

  @Test
  public void testSerializer() throws JsonProcessingException {
    String json = om.writeValueAsString(getErrorResponse());
    assertEquals(SERIALIZATION_RESULT, json);
  }

  @Test
  public void testDeserializer() throws IOException {
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

  private ObjectMapper getObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.configure(SerializationFeature.INDENT_OUTPUT, false);
    return om;
  }
}
