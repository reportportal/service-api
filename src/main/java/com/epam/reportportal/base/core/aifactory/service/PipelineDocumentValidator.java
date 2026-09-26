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
 * Checks a document a stage reports against the schema it names. Schemas live on the classpath as
 * {@code schema/pipeline/<documentSchemaName>.schema.json}; a reference without a schema is not checked, so
 * pipelines can attach documents of their own.
 */
@Component
@RequiredArgsConstructor
public class PipelineDocumentValidator {

  private static final String SCHEMA_BASE = "https://schema.reportportal.io/pipeline/";
  private static final String RESOURCE_BASE = "schema/pipeline/";
  private static final String SUFFIX = ".schema.json";
  private static final Pattern SAFE_REF = Pattern.compile("[A-Za-z0-9._-]{1,128}");
  static final int MAX_REASON_LENGTH = 1000;

  private final JsonSchemaValidator schemaValidator;
  private final ObjectMapper objectMapper;

  /**
   * @return why the document is not valid, or empty when it is valid or there is no schema to check it with
   */
  public Optional<String> validate(String documentSchemaName, Map<String, Object> document) {
    if (documentSchemaName == null || document == null || !SAFE_REF.matcher(documentSchemaName).matches()
        || !new ClassPathResource(RESOURCE_BASE + documentSchemaName + SUFFIX).exists()) {
      return Optional.empty();
    }
    try {
      var messages = schemaValidator.validate(SCHEMA_BASE + documentSchemaName + SUFFIX, objectMapper.valueToTree(document));
      if (messages.isEmpty()) {
        return Optional.empty();
      }
      var reason = "Document does not match schema '" + documentSchemaName + "': "
          + messages.stream().map(ValidationMessage::getMessage).sorted().collect(Collectors.joining("; "));
      return Optional.of(reason.length() > MAX_REASON_LENGTH ? reason.substring(0, MAX_REASON_LENGTH) : reason);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read schema '" + documentSchemaName + "'", e);
    }
  }
}
