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

import com.epam.reportportal.base.core.configs.JsonSchemaValidatorConfig;
import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Validates JSON documents against a JSON schema. This class uses the NetworkNT JSON Schema library to perform
 * validation. The schema is loaded from the classpath using a prefix mapping.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Component
public class JsonSchemaValidator {

  private final SchemaRegistry schemaRegistry;

  /**
   * Constructor for JsonSchemaValidator.
   *
   * @param config Configuration for the JSON schema validator
   */
  @Autowired
  public JsonSchemaValidator(JsonSchemaValidatorConfig config) {
    this.schemaRegistry = config.createSchemaRegistry();
  }

  /**
   * Validates a JSON document against a schema located at the specified location.
   *
   * @param location The location of the schema
   * @param input    The {@link String} JSON document to validate
   * @return validation errors, empty when the document is valid
   */
  public List<Error> validate(String location, String input) {
    Schema schema = schemaRegistry.getSchema(SchemaLocation.of(location));
    return schema.validate(input, InputFormat.JSON);
  }

  /**
   * Validates a JSON document against a schema located at the specified location.
   *
   * @param location The location of the schema
   * @param input    The {@link JsonNode} document to validate
   * @return validation errors, empty when the document is valid
   */
  public List<Error> validate(String location, JsonNode input) {
    Schema schema = schemaRegistry.getSchema(SchemaLocation.of(location));
    return schema.validate(input);
  }
}
