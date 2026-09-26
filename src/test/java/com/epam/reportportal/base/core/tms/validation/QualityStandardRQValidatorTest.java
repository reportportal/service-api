package com.epam.reportportal.base.core.tms.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers {@code sum(criteria[].maxPoints) == 100} enforced by {@link ValidQualityStandardRQ}.
 */
class QualityStandardRQValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    var factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  private TmsQualityStandardCriterionRQ criterion(int maxPoints) {
    return TmsQualityStandardCriterionRQ.builder()
        .name("criterion")
        .maxPoints(maxPoints)
        .sequence(1)
        .build();
  }

  @Test
  void shouldPassValidationWhenCriteriaMaxPointsSumToExactlyOneHundred() {
    var rq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of(criterion(25), criterion(20), criterion(20), criterion(15), criterion(10), criterion(10)))
        .build();

    var violations = validator.validate(rq);

    assertTrue(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenCriteriaMaxPointsSumBelowOneHundred() {
    var rq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of(criterion(25), criterion(20)))
        .build();

    var violations = validator.validate(rq);

    assertFalse(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenCriteriaMaxPointsSumAboveOneHundred() {
    var rq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of(criterion(60), criterion(60)))
        .build();

    var violations = validator.validate(rq);

    assertFalse(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenAnyCriterionMaxPointsExceedsOneHundred() {
    // A single criterion above 100 is rejected by @Max on the field itself, independent of
    // the class-level sum check - this is what keeps the sum computation (a long, see
    // QualityStandardRQValidator) from ever needing to reason about implausibly large inputs.
    var rq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of(criterion(101)))
        .build();

    var violations = validator.validate(rq);

    assertFalse(violations.isEmpty());
  }

  @Test
  void shouldFailValidationWhenCriteriaIsEmpty() {
    var rq = TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .criteria(List.of())
        .build();

    var violations = validator.validate(rq);

    assertFalse(violations.isEmpty());
  }
}
