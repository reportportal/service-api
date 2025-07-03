package com.epam.ta.reportportal.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestFolderIdentifierValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void shouldPassValidationWhenTestFolderIdIsProvided() {
    var request = TmsTestCaseTestFolderRQ.builder()
        .testFolderId(1L)
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldPassValidationWhenTestFolderNameIsProvided() {
    var request = TmsTestCaseTestFolderRQ.builder()
        .testFolderName("Test Folder")
        .build();

    var violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenBothFieldsAreProvided() {
    var request = TmsTestCaseTestFolderRQ.builder()
        .testFolderId(1L)
        .testFolderName("Test Folder")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.iterator().next().getMessage()
        .contains("Either testFolderId or testFolderName must be provided"));
  }

  @Test
  void shouldFailValidationWhenBothFieldsAreNull() {
    var request = TmsTestCaseTestFolderRQ.builder().build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.iterator().next().getMessage()
        .contains("Either testFolderId or testFolderName must be provided"));
  }

  @Test
  void shouldFailValidationWhenTestFolderNameIsEmpty() {
    var request = TmsTestCaseTestFolderRQ.builder()
        .testFolderName("")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenTestFolderNameIsBlank() {
    var request = TmsTestCaseTestFolderRQ.builder()
        .testFolderName("   ")
        .build();

    var violations = validator.validate(request);

    assertFalse(violations.isEmpty());
  }
}
