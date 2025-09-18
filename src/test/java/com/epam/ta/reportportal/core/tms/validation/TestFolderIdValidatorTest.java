package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.rules.exception.ReportPortalException;
import org.junit.jupiter.api.Test;

public class TestFolderIdValidatorTest {

  private final TestFolderIdValidator testFolderIdValidator = new TestFolderIdValidator();

  @Test
  void shouldPassValidationWhenOnlyTestFolderIdIsProvided() {
    // Given
    Long testFolderId = 1L;
    String testFolderName = null;

    // When/Then - should not throw exception
    testFolderIdValidator.validate(testFolderId, testFolderName);
  }

  @Test
  void shouldPassValidationWhenOnlyTestFolderNameIsProvided() {
    // Given
    Long testFolderId = null;
    String testFolderName = "Test Folder";

    // When/Then - should not throw exception
    testFolderIdValidator.validate(testFolderId, testFolderName);
  }

  @Test
  void shouldFailValidationWhenBothParametersAreProvided() {
    // Given
    Long testFolderId = 1L;
    String testFolderName = "Test Folder";

    // When/Then
    var exception = assertThrows(ReportPortalException.class, () ->
        testFolderIdValidator.validate(testFolderId, testFolderName));

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
        testFolderIdValidator.validate(testFolderId, testFolderName));

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
        testFolderIdValidator.validate(testFolderId, testFolderName));

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
        testFolderIdValidator.validate(testFolderId, testFolderName));

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
        testFolderIdValidator.validate(testFolderId, testFolderName));

    assertTrue(exception.getMessage()
        .contains("Either testFolderId or testFolderName must be provided and not empty"));
  }

  @Test
  void shouldPassValidationWhenTestFolderIdIsZero() {
    // Given
    Long testFolderId = 0L;
    String testFolderName = null;

    // When/Then - should not throw exception (0L is a valid ID)
    testFolderIdValidator.validate(testFolderId, testFolderName);
  }

  @Test
  void shouldPassValidationWhenTestFolderNameHasOnlyValidCharacters() {
    // Given
    Long testFolderId = null;
    String testFolderName = "Test-Folder_123 (New)";

    // When/Then - should not throw exception
    testFolderIdValidator.validate(testFolderId, testFolderName);
  }

  @Test
  void shouldPassValidationWhenTestFolderNameIsSingleCharacter() {
    // Given
    Long testFolderId = null;
    String testFolderName = "A";

    // When/Then - should not throw exception
    testFolderIdValidator.validate(testFolderId, testFolderName);
  }
}
