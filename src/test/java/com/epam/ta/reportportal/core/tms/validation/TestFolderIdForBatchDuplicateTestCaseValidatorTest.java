package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestFolderIdForBatchDuplicateTestCaseValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void shouldPassValidationWhenOnlyTestFolderIdIsProvided() {
    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L, 3L))
        .testFolderId(1L)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenOnlyTestFolderIsProvided() {
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder")
        .build();

    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L, 3L))
        .testFolder(testFolder)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenBothFolderFieldsAreNull() {
    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L, 3L))
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenBothFolderFieldsAreProvided() {
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder")
        .build();

    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L, 3L))
        .testFolderId(1L)
        .testFolder(testFolder)
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.iterator().next().getMessage()
        .contains("Either testFolderId or testFolderName must be provided and not empty"));
  }

  @Test
  void shouldPassValidationWhenTestFolderIdIsZero() {
    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L, 3L))
        .testFolderId(0L)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenTestCaseIdsIsEmpty() {
    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of())
        .testFolderId(1L)
        .build();

    var violations = validator.validate(request);

    // This should fail due to @NotEmpty on testCaseIds, not the folder validation
    assertFalse(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderHasParentId() {
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder")
        .parentTestFolderId(2L)
        .build();

    var request = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(List.of(1L, 2L, 3L))
        .testFolder(testFolder)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }
}
