package com.epam.ta.reportportal.core.events.annotations;

import javax.validation.Constraint;
import java.lang.annotation.*;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Documented
@Constraint(validatedBy = { WidgetLimitRangeValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER })
public @interface WidgetLimitRange {
	String message() default "The provided limit is not allowed for the widget";

	Class<?>[] groups() default {};

	Class<?>[] payload() default {};

	String[] allowedValues() default {};
}
