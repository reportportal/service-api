package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.util.JsonSchemaValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Checks the raw output a stage reports against the schema it names. Schemas live on the classpath as
 * {@code schema/pipeline/<rawOutputSchemaName>.schema.json}; a reference without a schema is not checked, so
 * pipelines can attach raw outputs of their own.
 */
@Component
@RequiredArgsConstructor
public class PipelineRawOutputValidator {

  private static final String SCHEMA_BASE = "https://schema.reportportal.io/pipeline/";
  private static final String RESOURCE_BASE = "schema/pipeline/";
  private static final String SUFFIX = ".schema.json";
  private static final Pattern SAFE_REF = Pattern.compile("[A-Za-z0-9._-]{1,128}");
  static final int MAX_REASON_LENGTH = 1000;

  private final JsonSchemaValidator schemaValidator;
  private final ObjectMapper objectMapper;

  /**
   * @return why the raw output is not valid, or empty when it is valid or there is no schema to check it with
   */
  public Optional<String> validate(String rawOutputSchemaName, Map<String, Object> rawOutput) {
    if (rawOutputSchemaName == null || rawOutput == null || !SAFE_REF.matcher(rawOutputSchemaName).matches()
        || !new ClassPathResource(RESOURCE_BASE + rawOutputSchemaName + SUFFIX).exists()) {
      return Optional.empty();
    }
    try {
      var messages = schemaValidator.validate(SCHEMA_BASE + rawOutputSchemaName + SUFFIX, objectMapper.valueToTree(rawOutput));
      if (messages.isEmpty()) {
        return Optional.empty();
      }
      var reason = "Raw output does not match schema '" + rawOutputSchemaName + "': "
          + messages.stream().map(ValidationMessage::getMessage).sorted().collect(Collectors.joining("; "));
      return Optional.of(reason.length() > MAX_REASON_LENGTH ? reason.substring(0, MAX_REASON_LENGTH) : reason);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read schema '" + rawOutputSchemaName + "'", e);
    }
  }
}
