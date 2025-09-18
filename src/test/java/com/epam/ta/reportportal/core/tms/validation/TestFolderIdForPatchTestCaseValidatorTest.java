package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestFolderIdForPatchTestCaseValidatorTest {

  private TestFolderIdForPatchTestCaseValidator validator;
  private ConstraintValidatorContext context;
  private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  void setUp() {
    validator = new TestFolderIdForPatchTestCaseValidator();
    context = mock(ConstraintValidatorContext.class);
    violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    // Setup mock for method chaining
    when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
    when(violationBuilder.addConstraintViolation()).thenReturn(context);
  }

  @Test
  void shouldReturnTrueForNullObject() {
    // When
    boolean result = validator.isValid(null, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenOnlyTestFolderIdIsProvided() {
    // Given
    var request = TmsTestCaseRQ.builder()
        .testFolderId(1L)
        .testFolder(null)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenOnlyTestFolderNameIsProvided() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder Name")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenBothTestFolderIdAndTestFolderNameAreNull() {
    // Given - for PATCH operation, both fields can be null (unlike UPSERT)
    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(null)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnFalseWhenBothTestFolderIdAndTestFolderNameAreProvided() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder Name")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(1L)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate(
        "Either testFolderId or testFolderName must be provided and not empty");
    verify(violationBuilder).addConstraintViolation();
  }

  @Test
  void shouldReturnTrueWhenTestFolderExistsButNameIsNull() {
    // Given - testFolder object exists but name is null, should be valid for PATCH
    var testFolder = NewTestFolderRQ.builder()
        .name(null)
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnFalseWhenTestFolderIdProvidedAndTestFolderExistsWithName() {
    // Given - both testFolderId and testFolder.name are provided
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(1L)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate(
        "Either testFolderId or testFolderName must be provided and not empty");
    verify(violationBuilder).addConstraintViolation();
  }

  @Test
  void shouldReturnTrueWhenTestFolderIdIsZero() {
    // Given
    var request = TmsTestCaseRQ.builder()
        .testFolderId(0L)
        .testFolder(null)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenTestFolderIdIsNegative() {
    // Given
    var request = TmsTestCaseRQ.builder()
        .testFolderId(-1L)
        .testFolder(null)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenTestFolderNameIsEmptyString() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenTestFolderNameIsWhitespace() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("   ")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result);
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnFalseWhenBothFieldsHaveValidNonNullValues() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("Valid Folder Name")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(42L)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertFalse(result);
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate(
        "Either testFolderId or testFolderName must be provided and not empty");
    verify(violationBuilder).addConstraintViolation();
  }

  @Test
  void shouldInitializeWithoutErrors() {
    // Given
    var annotation = mock(ValidTestFolderIdForPatchTestCase.class);

    // When & Then - should not throw any exception
    validator.initialize(annotation);
  }

  @Test
  void shouldReturnTrueWhenTestFolderExistsButEmpty() {
    // Given - testFolder object exists but fields are not set
    var testFolder = NewTestFolderRQ.builder().build(); // name will be null

    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result); // Should pass for PATCH because name is null (no conflict)
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnTrueWhenTestFolderIdProvidedAndTestFolderExistsButNameIsNull() {
    // Given - testFolderId is provided but testFolder.name is null (no conflict)
    var testFolder = NewTestFolderRQ.builder()
        .name(null)
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(1L)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertTrue(result); // Should pass because testFolder.name is null
    verifyNoInteractions(context);
  }

  @Test
  void shouldReturnFalseWhenTestFolderIdIsZeroAndTestFolderNameProvided() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(0L)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertFalse(result); // Should fail because both are provided
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate(
        "Either testFolderId or testFolderName must be provided and not empty");
    verify(violationBuilder).addConstraintViolation();
  }

  @Test
  void shouldReturnFalseWhenTestFolderIdIsNegativeAndTestFolderNameProvided() {
    // Given
    var testFolder = NewTestFolderRQ.builder()
        .name("Test Folder")
        .build();

    var request = TmsTestCaseRQ.builder()
        .testFolderId(-5L)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertFalse(result); // Should fail because both are provided
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate(
        "Either testFolderId or testFolderName must be provided and not empty");
    verify(violationBuilder).addConstraintViolation();
  }
}
