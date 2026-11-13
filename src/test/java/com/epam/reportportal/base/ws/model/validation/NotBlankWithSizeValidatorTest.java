/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.ws.model.validation;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.epam.reportportal.base.infrastructure.annotations.NotBlankWithSize;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:tatyana_gladysheva@epam.com">Tatyana Gladysheva</a>
 */
public class NotBlankWithSizeValidatorTest {

  private static final String NOT_NULL_PROPERTY = "Field ''{0}'' should not be null.";
  private static final String NOT_BLANK_PROPERTY = "Field ''{0}'' should not contain only white spaces and shouldn''t be empty.";
  private static final String SIZE_PROPERTY = "Field ''{0}'' should have size from ''{2}'' to ''{1}''.";

  private static final String VALID_NAME = "Valid name";
  private static final String SPACE = " ";
  private static final String WHITESPACES_NAME = "                   ";
  private static final String INVALID_NAME_WITH_LESS_CHARACTERS = "cc";
  private static final String INVALID_NAME_WITH_MORE_CHARACTERS =
      "tttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
          + "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt";
  private static final String VALID_NAME_WITH_NUMBER_OF_CHARS_EQUAL_TO_MIN = "ccc";
  private static final String VALID_NAME_WITH_NUMBER_OF_CHARS_EQUAL_TO_MAX =
      "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt"
          + "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt";

  private static Validator validator;

  @BeforeAll
  public static void init() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  public void validateShouldReturnNoViolationsWhenNameIsValidAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(VALID_NAME);

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void validateShouldReturnOneViolationWithNotNullMessageWhenNameIsNullAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(null);

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnFieldClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(NOT_NULL_PROPERTY));
  }

  @Test
  public void validateShouldReturnOneViolationWithNotBlankMessageWhenNameIsEmptyAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(EMPTY);

    String expectedMessage = NOT_BLANK_PROPERTY + SPACE + SIZE_PROPERTY;

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnFieldClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void validateShouldReturnOneViolationWithNotBlankMessageWhenNameConsistsOfWhitespacesAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(WHITESPACES_NAME);

    String expectedMessage = NOT_BLANK_PROPERTY + SPACE + SIZE_PROPERTY;

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnFieldClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void validateShouldReturnOneViolationWithSizeMessageWhenNameHasLessThanMinNumberOfCharactersAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(INVALID_NAME_WITH_LESS_CHARACTERS);

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnFieldClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(SIZE_PROPERTY));
  }

  @Test
  public void validateShouldReturnOneViolationWithSizeMessageWhenNameHasMoreThanMaxNumberOfCharactersAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(INVALID_NAME_WITH_MORE_CHARACTERS);

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnFieldClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(SIZE_PROPERTY));
  }

  @Test
  public void validateShouldReturnNoViolationsWhenNameHasNumberOfCharactersEqualToMinAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(VALID_NAME_WITH_NUMBER_OF_CHARS_EQUAL_TO_MIN);

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void validateShouldReturnNoViolationsWhenNameHasNumberOfCharactersEqualToMaxAndValidationIsOnField() {
    //GIVEN
    AnnotationOnFieldClass testObject = new AnnotationOnFieldClass();
    testObject.setName(VALID_NAME_WITH_NUMBER_OF_CHARS_EQUAL_TO_MAX);

    //WHEN
    Set<ConstraintViolation<AnnotationOnFieldClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.isEmpty(), is(true));
  }

  @Getter
  private static class AnnotationOnFieldClass {

    @NotBlankWithSize(min = 3, max = 128)
    private String name;

    void setName(String name) {
      this.name = name;
    }
  }

  @Test
  public void validateShouldReturnNoViolationsWhenNameIsValidAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(VALID_NAME);

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void validateShouldReturnOneViolationWithNotNullMessageWhenNameIsNullAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(null);

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnMethodClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(NOT_NULL_PROPERTY));
  }

  @Test
  public void validateShouldReturnOneViolationWithNotBlankMessageWhenNameIsEmptyAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(EMPTY);

    String expectedMessage = NOT_BLANK_PROPERTY + SPACE + SIZE_PROPERTY;

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnMethodClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void validateShouldReturnOneViolationWithNotBlankMessageWhenNameConsistsOfWhitespacesAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(WHITESPACES_NAME);

    String expectedMessage = NOT_BLANK_PROPERTY + SPACE + SIZE_PROPERTY;

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnMethodClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(expectedMessage));
  }

  @Test
  public void validateShouldReturnOneViolationWithSizeMessageWhenNameHasLessThanMinNumberOfCharactersAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(INVALID_NAME_WITH_LESS_CHARACTERS);

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnMethodClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(SIZE_PROPERTY));
  }

  @Test
  public void validateShouldReturnOneViolationWithSizeMessageWhenNameHasMoreThanMaxNumberOfCharactersAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(INVALID_NAME_WITH_MORE_CHARACTERS);

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.size(), is(1));

    ConstraintViolation<AnnotationOnMethodClass> violation = violations.iterator().next();
    String actualMessage = violation.getMessage();

    assertThat(actualMessage, is(SIZE_PROPERTY));
  }

  @Test
  public void validateShouldReturnNoViolationsWhenNameHasNumberOfCharactersEqualToMinAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(VALID_NAME_WITH_NUMBER_OF_CHARS_EQUAL_TO_MIN);

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.isEmpty(), is(true));
  }

  @Test
  public void validateShouldReturnNoViolationsWhenNameHasNumberOfCharactersEqualToMaxAndValidationIsOnMethod() {
    //GIVEN
    AnnotationOnMethodClass testObject = new AnnotationOnMethodClass();
    testObject.setName(VALID_NAME_WITH_NUMBER_OF_CHARS_EQUAL_TO_MAX);

    //WHEN
    Set<ConstraintViolation<AnnotationOnMethodClass>> violations = validator.validate(testObject);

    //THEN
    assertThat(violations.isEmpty(), is(true));
  }

  private static class AnnotationOnMethodClass {

    private String name;

    @NotBlankWithSize(min = 3, max = 128)
    public String getName() {
      return name;
    }

    void setName(String name) {
      this.name = name;
    }
  }
}
