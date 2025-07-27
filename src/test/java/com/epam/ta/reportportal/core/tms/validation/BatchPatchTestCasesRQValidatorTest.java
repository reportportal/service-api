package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchPatchTestCasesRQValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void shouldPassValidationWhenAllFieldsAreProvided() {
    var tag = new TmsTestCaseAttributeRQ();
    tag.setValue("tag1");
    tag.setAttributeId(1L);

    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .priority("HIGH")
        .tags(List.of(tag))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyTestFolderIdIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .priority(null)
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyPriorityIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("HIGH")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyTagsIsProvided() {
    var tag = new TmsTestCaseAttributeRQ();
    tag.setValue("tag1");
    tag.setAttributeId(1L);

    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority(null)
        .tags(List.of(tag))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderIdAndPriorityAreProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .priority("HIGH")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderIdAndTagsAreProvided() {
    var tag = new TmsTestCaseAttributeRQ();
    tag.setValue("tag1");
    tag.setAttributeId(1L);

    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .priority(null)
        .tags(List.of(tag))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenPriorityAndTagsAreProvided() {
    var tag = new TmsTestCaseAttributeRQ();
    tag.setValue("tag1");
    tag.setAttributeId(1L);

    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("HIGH")
        .tags(List.of(tag))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenAllFieldsAreNull() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority(null)
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priroty or tags must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenPriorityIsEmpty() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priroty or tags must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenPriorityIsBlank() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("   ")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priroty or tags must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWhenObjectIsNull() {
    // Validator should return true for null objects (let @NotNull handle it)
    var validator = new BatchPatchTestCasesRQValidator();

    boolean result = validator.isValid(null, null);

    assertTrue(result);
  }

  @Test
  void shouldPassValidationWhenTagsListIsEmpty() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority(null)
        .tags(Collections.emptyList()) // Empty list, but not null
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTagsListIsEmptyButOtherFieldsAreProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .priority("HIGH")
        .tags(Collections.emptyList())
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenOnlyEmptyPriorityIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priroty or tags must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenOnlyBlankPriorityIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("   ")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priroty or tags must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWhenPriorityHasValidValue() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .priority("MEDIUM")
        .tags(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
