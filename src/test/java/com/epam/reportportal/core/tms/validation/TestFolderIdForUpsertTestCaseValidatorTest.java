package com.epam.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseRQ;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestFolderIdForUpsertTestCaseValidatorTest {

  private TestFolderIdForUpsertTestCaseValidator validator;
  private ConstraintValidatorContext context;
  private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  void setUp() {
    validator = new TestFolderIdForUpsertTestCaseValidator();
    context = mock(ConstraintValidatorContext.class);
    violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    // Setup mocks for method chaining
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
  void shouldReturnFalseWhenBothTestFolderIdAndTestFolderNameAreNull() {
    // Given
    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(null)
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
  void shouldReturnFalseWhenTestFolderExistsButNameIsNull() {
    // Given
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
    var annotation = mock(ValidTestFolderIdForUpsertTestCase.class);

    // When & Then - should not throw any exception
    validator.initialize(annotation);
  }

  @Test
  void shouldReturnFalseWhenTestFolderExistsButEmpty() {
    // Given - testFolder object exists but fields are not set
    var testFolder = NewTestFolderRQ.builder().build(); // name will be null

    var request = TmsTestCaseRQ.builder()
        .testFolderId(null)
        .testFolder(testFolder)
        .build();

    // When
    boolean result = validator.isValid(request, context);

    // Then
    assertFalse(result); // Should fail because name == null
    verify(context).disableDefaultConstraintViolation();
    verify(context).buildConstraintViolationWithTemplate(
        "Either testFolderId or testFolderName must be provided and not empty");
    verify(violationBuilder).addConstraintViolation();
  }
}
