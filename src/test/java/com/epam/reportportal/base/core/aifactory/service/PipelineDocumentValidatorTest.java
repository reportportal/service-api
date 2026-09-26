package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.configs.JsonSchemaValidatorConfig;
import com.epam.reportportal.base.util.JsonSchemaValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PipelineDocumentValidatorTest {

  private PipelineDocumentValidator validator;

  @BeforeEach
  void setUp() {
    var config = new JsonSchemaValidatorConfig();
    ReflectionTestUtils.setField(config, "schemaVersion", "V7");
    ReflectionTestUtils.setField(config, "schemaSource", "https://schema.reportportal.io/");
    ReflectionTestUtils.setField(config, "schemaLocation", "classpath:schema/");
    validator = new PipelineDocumentValidator(new JsonSchemaValidator(config), new ObjectMapper());
  }

  @Test
  void validate_WithAValidGradeDocument_ShouldHaveNoReason() {
    var document = Map.<String, Object>of("total_score", 83, "warnings", List.of("w"),
        "cases", List.of(Map.of("name", "Case", "total_score", 42)));

    assertTrue(validator.validate("grade", document).isEmpty());
  }

  @Test
  void validate_WithAGradeDocumentWithoutSuiteScore_ShouldNameTheProblem() {
    var reason = validator.validate("grade", Map.of("warnings", List.of()));

    assertTrue(reason.isPresent());
    assertTrue(reason.get().contains("total_score"), reason.get());
  }

  @Test
  void validate_WithAWronglyTypedCaseScore_ShouldBeRejected() {
    var document = Map.<String, Object>of("total_score", 83,
        "cases", List.of(Map.of("name", "Case", "total_score", "high")));

    assertTrue(validator.validate("grade", document).isPresent());
  }

  @Test
  void validate_WithoutASchemaOrDocumentOrWithAnUnsafeReference_ShouldNotCheck() {
    assertTrue(validator.validate("no-such-schema", Map.of("a", 1)).isEmpty());
    assertTrue(validator.validate(null, Map.of("a", 1)).isEmpty());
    assertTrue(validator.validate("grade", null).isEmpty());
    assertTrue(validator.validate("../grade", Map.of("a", 1)).isEmpty());
  }
}
