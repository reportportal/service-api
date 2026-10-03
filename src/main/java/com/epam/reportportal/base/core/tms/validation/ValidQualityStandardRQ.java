package com.epam.reportportal.base.core.tms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a {@code TmsQualityStandardRQ}'s criteria max points sum to 100.
 */
@Documented
@Constraint(validatedBy = QualityStandardRQValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQualityStandardRQ {

  String message() default "Criteria max points must sum to 100";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
