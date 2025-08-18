package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TmsAttributeRQValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void shouldPassValidationWhenOnlyIdIsProvided() {
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey(null);

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyKeyIsProvided() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("test-key");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenIdIsProvidedAndKeyIsEmpty() {
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey("");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenIdIsProvidedAndKeyIsBlank() {
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey("   ");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenKeyIsProvidedAndIdIsNull() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("valid-key");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenBothIdAndKeyAreProvided() {
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey("test-key");

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenNeitherIdNorKeyAreProvided() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey(null);

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenIdIsNullAndKeyIsEmpty() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("");

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenIdIsNullAndKeyIsBlank() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("   ");

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenBothIdAndKeyAreProvidedWithNonBlankKey() {
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey("non-blank-key");

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenBothIdAndKeyAreProvidedWithEmptyKey() {
    // This should pass because empty key is treated as no key
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey("");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenObjectIsNull() {
    // Validator should return true for null objects (let @NotNull handle it)
    var validator = new TmsAttributeRQValidator();

    boolean result = validator.isValid(null, null);

    assertTrue(result);
  }

  @Test
  void shouldPassValidationWhenOnlyValidKeyIsProvided() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("valid-key-123");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyValidIdIsProvided() {
    var request = new TmsAttributeRQ();
    request.setId(999L);
    request.setKey(null);

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenAllFieldsAreInvalid() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("");

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenOnlyBlankKeyIsProvided() {
    var request = new TmsAttributeRQ();
    request.setId(null);
    request.setKey("    ");

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either tag id or tag key must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWithValueFieldSet() {
    var request = new TmsAttributeRQ();
    request.setId(1L);
    request.setKey(null);
    request.setValue("some-value");

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
