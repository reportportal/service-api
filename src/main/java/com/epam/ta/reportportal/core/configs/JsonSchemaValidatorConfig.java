package com.epam.ta.reportportal.core.configs;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for JSON schema validation.
 * This class provides a method to create a {@link JsonSchemaFactory} instance
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

  public JsonSchemaFactory createSchemaFactory() {
    return JsonSchemaFactory.getInstance(
        VersionFlag.valueOf(schemaVersion),
        builder -> builder.schemaMappers(schemaMappers -> schemaMappers.mapPrefix(
            schemaSource,
            schemaLocation
        ))
    );
  }
}
