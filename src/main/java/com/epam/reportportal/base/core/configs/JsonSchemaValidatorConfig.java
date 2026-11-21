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

import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JSON schema validation. This class provides a method to create a {@link SchemaRegistry}
 * instance
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Configuration
public class JsonSchemaValidatorConfig {

  @Value("${rp.schema.specification}")
  private String schemaVersion;

  @Value("${rp.schema.source}")
  private String schemaSource;

  @Value("${rp.schema.location}")
  private String schemaLocation;

  /**
   * Creates a {@link SchemaRegistry} instance with the specified schema version and schema mappers.
   *
   * @return a {@link SchemaRegistry} instance
   */
  public SchemaRegistry createSchemaRegistry() {
    return SchemaRegistry.withDefaultDialect(SpecificationVersion.valueOf(schemaVersion),
        builder -> builder.schemaIdResolvers(resolvers -> resolvers.mapPrefix(schemaSource, schemaLocation)));
  }
}
