package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TestFolderIdentifierValidatorTest {

  private Validator validator;
  private TestFolderIdentifierValidator testFolderIdentifierValidator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
    testFolderIdentifierValidator = new TestFolderIdentifierValidator();
  }

  @Nested
  class TmsTestCaseRQValidationTests {

    @Test
    void shouldPassValidationWhenOnlyTestFolderIdIsProvided() {
      var request = TmsTestCaseRQ.builder()
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

      var request = TmsTestCaseRQ.builder()
          .testFolder(testFolder)
          .build();

      var violations = validator.validate(request);

      assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenBothFieldsAreNull() {
      var request = TmsTestCaseRQ.builder().build();

      var violations = validator.validate(request);

      assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenBothFieldsAreProvided() {
      var testFolder = NewTestFolderRQ.builder()
          .name("Test Folder")
          .build();

      var request = TmsTestCaseRQ.builder()
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
    void shouldFailValidationWhenBothFieldsAreProvidedWithEmptyTestFolder() {
      var testFolder = NewTestFolderRQ.builder().build();

      var request = TmsTestCaseRQ.builder()
          .testFolderId(1L)
          .testFolder(testFolder)
          .build();

      var violations = validator.validate(request);

      assertFalse(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenTestFolderIdIsZero() {
      var request = TmsTestCaseRQ.builder()
          .testFolderId(0L)
          .build();

      var violations = validator.validate(request);

      assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenTestFolderIdIsNegative() {
      var request = TmsTestCaseRQ.builder()
          .testFolderId(-1L)
          .build();

      var violations = validator.validate(request);

      assertTrue(violations.isEmpty());
    }
  }

  @Nested
  class BatchDuplicateTestCasesRQValidationTests {

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
    void shouldFailValidationWhenBothFieldsAreProvidedWithEmptyTestFolder() {
      var testFolder = NewTestFolderRQ.builder().build();

      var request = BatchDuplicateTestCasesRQ.builder()
          .testCaseIds(List.of(1L, 2L, 3L))
          .testFolderId(1L)
          .testFolder(testFolder)
          .build();

      var violations = validator.validate(request);

      assertFalse(violations.isEmpty());
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

  @Nested
  class UnsupportedTypeTests {

    @Test
    void shouldThrowExceptionForUnsupportedType() {
      var unsupportedObject = "This is not a supported type";

      var exception = assertThrows(IllegalStateException.class, () ->
          testFolderIdentifierValidator.isValid(unsupportedObject, null));

      assertTrue(exception.getMessage().contains("Wrong class annotated with @ValidTestFolderIdentifier"));
      assertTrue(exception.getMessage().contains("java.lang.String"));
    }

    @Test
    void shouldThrowExceptionForNullButWithWrongContextUsage() {
      // This tests the edge case where someone might try to validate an unsupported type
      var someCustomObject = new Object();

      var exception = assertThrows(IllegalStateException.class, () ->
          testFolderIdentifierValidator.isValid(someCustomObject, null));

      assertTrue(exception.getMessage().contains("Wrong class annotated with @ValidTestFolderIdentifier"));
    }
  }

  @Nested
  class DirectValidationMethodTests {

    @Test
    void shouldPassValidationWhenOnlyTestFolderIdIsProvided() {
      // Given
      Long testFolderId = 1L;
      String testFolderName = null;

      // When/Then - should not throw exception
      testFolderIdentifierValidator.validate(testFolderId, testFolderName);
    }

    @Test
    void shouldPassValidationWhenOnlyTestFolderNameIsProvided() {
      // Given
      Long testFolderId = null;
      String testFolderName = "Test Folder";

      // When/Then - should not throw exception
      testFolderIdentifierValidator.validate(testFolderId, testFolderName);
    }

    @Test
    void shouldFailValidationWhenBothParametersAreProvided() {
      // Given
      Long testFolderId = 1L;
      String testFolderName = "Test Folder";

      // When/Then
      var exception = assertThrows(ReportPortalException.class, () ->
          testFolderIdentifierValidator.validate(testFolderId, testFolderName));

      assertTrue(exception.getMessage()
          .contains("Either testFolderId or testFolderName must be provided and not empty"));
    }

    @Test
    void shouldFailValidationWhenBothParametersAreNull() {
      // Given
      Long testFolderId = null;
      String testFolderName = null;

      // When/Then
      var exception = assertThrows(ReportPortalException.class, () ->
          testFolderIdentifierValidator.validate(testFolderId, testFolderName));

      assertTrue(exception.getMessage()
          .contains("Either testFolderId or testFolderName must be provided and not empty"));
    }

    @Test
    void shouldFailValidationWhenTestFolderNameIsEmpty() {
      // Given
      Long testFolderId = null;
      String testFolderName = "";

      // When/Then
      var exception = assertThrows(ReportPortalException.class, () ->
          testFolderIdentifierValidator.validate(testFolderId, testFolderName));

      assertTrue(exception.getMessage()
          .contains("Either testFolderId or testFolderName must be provided and not empty"));
    }

    @Test
    void shouldFailValidationWhenTestFolderNameIsBlank() {
      // Given
      Long testFolderId = null;
      String testFolderName = "   ";

      // When/Then
      var exception = assertThrows(ReportPortalException.class, () ->
          testFolderIdentifierValidator.validate(testFolderId, testFolderName));

      assertTrue(exception.getMessage()
          .contains("Either testFolderId or testFolderName must be provided and not empty"));
    }

    @Test
    void shouldFailValidationWhenTestFolderNameIsWhitespaceOnly() {
      // Given
      Long testFolderId = null;
      String testFolderName = "\t\n\r ";

      // When/Then
      var exception = assertThrows(ReportPortalException.class, () ->
          testFolderIdentifierValidator.validate(testFolderId, testFolderName));

      assertTrue(exception.getMessage()
          .contains("Either testFolderId or testFolderName must be provided and not empty"));
    }

    @Test
    void shouldPassValidationWhenTestFolderIdIsZero() {
      // Given
      Long testFolderId = 0L;
      String testFolderName = null;

      // When/Then - should not throw exception (0L is a valid ID)
      testFolderIdentifierValidator.validate(testFolderId, testFolderName);
    }

    @Test
    void shouldPassValidationWhenTestFolderNameHasOnlyValidCharacters() {
      // Given
      Long testFolderId = null;
      String testFolderName = "Test-Folder_123 (New)";

      // When/Then - should not throw exception
      testFolderIdentifierValidator.validate(testFolderId, testFolderName);
    }

    @Test
    void shouldPassValidationWhenTestFolderNameIsSingleCharacter() {
      // Given
      Long testFolderId = null;
      String testFolderName = "A";

      // When/Then - should not throw exception
      testFolderIdentifierValidator.validate(testFolderId, testFolderName);
    }
  }
}
