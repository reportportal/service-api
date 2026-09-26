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

class PipelineRawOutputValidatorTest {

  private PipelineRawOutputValidator validator;

  @BeforeEach
  void setUp() {
    var config = new JsonSchemaValidatorConfig();
    ReflectionTestUtils.setField(config, "schemaVersion", "V7");
    ReflectionTestUtils.setField(config, "schemaSource", "https://schema.reportportal.io/");
    ReflectionTestUtils.setField(config, "schemaLocation", "classpath:schema/");
    validator = new PipelineRawOutputValidator(new JsonSchemaValidator(config), new ObjectMapper());
  }

  @Test
  void validate_WithAValidGradeRawOutput_ShouldHaveNoReason() {
    var rawOutput = Map.<String, Object>of("total_score", 83, "warnings", List.of("w"),
        "cases", List.of(Map.of("name", "Case", "total_score", 42)));

    assertTrue(validator.validate("grade", rawOutput).isEmpty());
  }

  @Test
  void validate_WithAGradeRawOutputWithoutSuiteScore_ShouldNameTheProblem() {
    var reason = validator.validate("grade", Map.of("warnings", List.of()));

    assertTrue(reason.isPresent());
    assertTrue(reason.get().contains("total_score"), reason.get());
  }

  @Test
  void validate_WithAWronglyTypedCaseScore_ShouldBeRejected() {
    var rawOutput = Map.<String, Object>of("total_score", 83,
        "cases", List.of(Map.of("name", "Case", "total_score", "high")));

    assertTrue(validator.validate("grade", rawOutput).isPresent());
  }

  @Test
  void validate_WithoutASchemaOrRawOutputOrWithAnUnsafeReference_ShouldNotCheck() {
    assertTrue(validator.validate("no-such-schema", Map.of("a", 1)).isEmpty());
    assertTrue(validator.validate(null, Map.of("a", 1)).isEmpty());
    assertTrue(validator.validate("grade", null).isEmpty());
    assertTrue(validator.validate("../grade", Map.of("a", 1)).isEmpty());
  }
}
