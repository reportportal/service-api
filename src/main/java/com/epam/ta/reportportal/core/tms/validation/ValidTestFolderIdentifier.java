package com.epam.ta.reportportal.core.tms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure that either testFolderId or testFolderName is provided.
 * <p>
 * This annotation validates that at least one of the two fields is not null, preventing
 * scenarios where both fields are null which would make the request invalid.
 * </p>
 */
@Documented
@Constraint(validatedBy = TestFolderIdentifierValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTestFolderIdentifier {

  /**
   * The error message to be returned when validation fails.
   *
   * @return the error message
   */
  String message() default "Either testFolderId or testFolderName must be provided";

  /**
   * Validation groups.
   *
   * @return the validation groups
   */
  Class<?>[] groups() default {};

  /**
   * Payload for extensibility purposes.
   *
   * @return the payload
   */
  Class<? extends Payload>[] payload() default {};
}
