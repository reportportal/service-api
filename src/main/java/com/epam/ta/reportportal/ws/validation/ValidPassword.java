/*
 * Copyright 2025 EPAM Systems
 */
package com.epam.ta.reportportal.ws.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Bean Validation constraint for user passwords.
 *
 */
@Documented
@Constraint(validatedBy = ValidPasswordValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidPassword {

  String DEFAULT_MESSAGE = "Password must meet server-defined minimum length, include a digit, "
      + "special symbol, uppercase & lowercase letter, no whitespace, max 256 characters.";

  String message() default DEFAULT_MESSAGE;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  boolean allowNull() default false;
}
