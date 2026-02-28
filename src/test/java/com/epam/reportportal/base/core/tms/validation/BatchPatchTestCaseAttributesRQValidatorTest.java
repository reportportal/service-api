package com.epam.reportportal.base.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchPatchTestCaseAttributesRQValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void shouldPassValidationWhenAllFieldsAreProvided() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Set.of("key1", "key2"))
        .attributeKeysToRemove(Set.of("key3", "key4"))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyAttributesToAddIsProvided() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Set.of("key1", "key2"))
        .attributeKeysToRemove(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyAttributesToRemoveIsProvided() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(null)
        .attributeKeysToRemove(Set.of("key3", "key4"))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenAllFieldsAreNull() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(null)
        .attributeKeysToRemove(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either attributes to add or to remove must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenAllFieldsAreEmpty() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Collections.emptySet())
        .attributeKeysToRemove(Collections.emptySet())
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either attributes to add or to remove must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenAttributesToAddIsEmptyAndAttributesToRemoveIsNull() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Collections.emptySet())
        .attributeKeysToRemove(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either attributes to add or to remove must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenAttributesToAddIsNullAndAttributesToRemoveIsEmpty() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(null)
        .attributeKeysToRemove(Collections.emptySet())
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either attributes to add or to remove must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWhenObjectIsNull() {
    // Validator should return true for null objects (let @NotNull handle it)
    var validator = new BatchPatchTestCaseAttributesRQValidator();

    boolean result = validator.isValid(null, null);

    assertTrue(result);
  }

  @Test
  void shouldPassValidationWhenAttributesToAddHasSingleElement() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Set.of("key1"))
        .attributeKeysToRemove(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenAttributesToRemoveHasSingleElement() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(null)
        .attributeKeysToRemove(Set.of("key3"))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenOnlyEmptyListsAreProvided() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Collections.emptySet())
        .attributeKeysToRemove(Collections.emptySet())
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either attributes to add or to remove must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWhenBothListsHaveValidValues() {
    var request = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .attributeKeysToAdd(Set.of("key5", "key6", "key7"))
        .attributeKeysToRemove(Set.of("key1", "key2"))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
