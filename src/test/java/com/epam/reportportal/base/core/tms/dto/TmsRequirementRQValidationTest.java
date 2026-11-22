/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.core.tms.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TmsRequirementRQValidationTest {

  private static jakarta.validation.ValidatorFactory factory;
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    factory.close();
  }

  @Test
  void shouldAcceptRequirementWithoutId() {
    assertThat(validator.validate(TmsRequirementRQ.builder().value("value").build())).isEmpty();
  }

  @Test
  void shouldAcceptRequirementWithIdAndMaxLengthValue() {
    var rq = TmsRequirementRQ.builder().id("x".repeat(255)).value("y".repeat(255)).build();
    assertThat(validator.validate(rq)).isEmpty();
  }

  @Test
  void shouldRejectBlankOrMissingValue() {
    assertThat(validator.validate(TmsRequirementRQ.builder().build())).hasSize(1);
    assertThat(validator.validate(TmsRequirementRQ.builder().value("  ").build())).hasSize(1);
  }

  @Test
  void shouldRejectTooLongValue() {
    var violations = validator.validate(TmsRequirementRQ.builder().value("y".repeat(256)).build());
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getPropertyPath()).hasToString("value");
  }

  @Test
  void shouldRejectTooLongId() {
    var violations =
        validator.validate(TmsRequirementRQ.builder().id("x".repeat(256)).value("v").build());
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getPropertyPath()).hasToString("id");
  }
}
