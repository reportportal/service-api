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

import com.epam.ta.reportportal.core.configs.JsonSchemaValidatorConfig;
import com.epam.ta.reportportal.core.plugin.PluginInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates JSON documents against a JSON schema.
 * This class uses the NetworkNT JSON Schema library to perform validation.
 * The schema is loaded from the classpath using a prefix mapping.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Component
public class JsonSchemaValidator {

  private final JsonSchemaFactory schemaFactory;

  /**
   * Constructor for JsonSchemaValidator.
   *
   * @param config Configuration for the JSON schema validator
   */
  @Autowired
  public JsonSchemaValidator(JsonSchemaValidatorConfig config) {
    this.schemaFactory = config.createSchemaFactory();
  }

  /**
   * Validates a JSON document against a schema located at the specified location.
   *
   * @param location The location of the schema
   * @param input    The {@link String} JSON document to validate
   * @return A {@link Set} of {@link ValidationMessage} indicating any validation errors
   * @throws IOException If an error occurs while reading the schema or validating the input
   */
  public Set<ValidationMessage> validate(String location, String input) throws IOException {
    JsonSchema schema = schemaFactory.getSchema(SchemaLocation.of(location));
    return schema.validate(input, InputFormat.JSON);
  }

  /**
   * Validates a JSON document against a schema located at the specified location.
   *
   * @param location The location of the schema
   * @param input    The {@link JsonNode} document to validate
   * @return A {@link Set} of {@link ValidationMessage} indicating any validation errors
   * @throws IOException If an error occurs while reading the schema or validating the input
   */
  public Set<ValidationMessage> validate(String location, JsonNode input) throws IOException {
    JsonSchema schema = schemaFactory.getSchema(SchemaLocation.of(location));
    return schema.validate(input);
  }
}
