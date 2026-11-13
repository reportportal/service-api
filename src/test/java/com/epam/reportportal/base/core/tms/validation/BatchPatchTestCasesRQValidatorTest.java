package com.epam.reportportal.base.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCasesRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
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
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .priority("HIGH")
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
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyTestFolderIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolder(NewTestFolderRQ.builder().name("New Folder").build())
        .priority(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderAndPriorityAreProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolder(NewTestFolderRQ.builder().name("New Folder").build())
        .priority("HIGH")
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderWithParentIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolder(NewTestFolderRQ.builder()
            .name("Nested Folder")
            .parentTestFolderId(5L)
            .build())
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenBothTestFolderIdAndTestFolderAreProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .testFolder(NewTestFolderRQ.builder().name("New Folder").build())
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either testFolderId or testFolderName must be provided")));
  }

  @Test
  void shouldFailValidationWhenBothTestFolderIdAndTestFolderWithPriorityAreProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .testFolder(NewTestFolderRQ.builder().name("New Folder").build())
        .priority("HIGH")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either testFolderId or testFolderName must be provided")));
  }

  @Test
  void shouldFailValidationWhenAllFieldsAreNull() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .testFolder(null)
        .priority(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenPriorityIsEmpty() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .testFolder(null)
        .priority("")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenPriorityIsBlank() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .testFolder(null)
        .priority("   ")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenTestFolderHasNullName() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolder(NewTestFolderRQ.builder().name(null).build())
        .priority(null)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenTestFolderHasNullNameButPriorityIsBlank() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolder(NewTestFolderRQ.builder().name(null).build())
        .priority("   ")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWhenTestFolderHasNullNameButPriorityIsValid() {
    // testFolder with null name is ignored, but priority is valid
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolder(NewTestFolderRQ.builder().name(null).build())
        .priority("HIGH")
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenObjectIsNull() {
    // Validator should return true for null objects (let @NotNull handle it)
    var validatorInstance = new BatchPatchTestCasesRQValidator();

    var result = validatorInstance.isValid(null, null);

    assertTrue(result);
  }

  @Test
  void shouldFailValidationWhenOnlyEmptyPriorityIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .testFolder(null)
        .priority("")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldFailValidationWhenOnlyBlankPriorityIsProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .testFolder(null)
        .priority("   ")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v ->
        v.getMessage().contains("Either folderId or priority must be provided and not empty")));
  }

  @Test
  void shouldPassValidationWhenPriorityHasValidValue() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(null)
        .testFolder(null)
        .priority("MEDIUM")
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderIdAndPriorityAreProvided() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .testFolder(null)
        .priority("LOW")
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldNotTriggerConflictWhenTestFolderIsNullButTestFolderIdExists() {
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .testFolder(null)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldNotTriggerConflictWhenTestFolderNameIsNullButTestFolderIdExists() {
    // testFolder exists but name is null - should not trigger conflict
    var request = BatchPatchTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L))
        .testFolderId(1L)
        .testFolder(NewTestFolderRQ.builder().name(null).parentTestFolderId(5L).build())
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
