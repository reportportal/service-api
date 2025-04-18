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

package com.epam.ta.reportportal.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.core.configs.JsonSchemaValidatorConfig;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test class for {@link JsonSchemaValidator}.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@SpringBootTest
@ContextConfiguration(classes = {JsonSchemaValidatorConfig.class, JsonSchemaValidator.class})
public class JsonSchemaValidatorTest {

  @Autowired
  private JsonSchemaValidator jsonSchemaValidator;
  @Autowired
  private Environment environment;

  @Test
  public void testValidateValidJson() throws IOException {
    String schemaLocation = environment.getProperty("rp.schema.source") + "test-schema.json";
    String validJson = "{ \"name\": \"John\", \"age\": 30 }";

    Set<ValidationMessage> messages = jsonSchemaValidator.validate(schemaLocation, validJson);
    assertTrue(messages.isEmpty(), "Validation should pass for valid JSON");
  }

  @Test
  public void testValidateInvalidJson() throws IOException {
    String schemaLocation = environment.getProperty("rp.schema.source") + "test-schema.json";
    String invalidJson = "{ \"name\": \"John\", \"age\": \"thirty\" }";

    Set<ValidationMessage> messages = jsonSchemaValidator.validate(schemaLocation, invalidJson);
    assertFalse(messages.isEmpty(), "Validation should pass for valid JSON");
  }
}
