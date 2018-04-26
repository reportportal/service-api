package com.epam.ta.reportportal.store.service;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PersistedProperty {
	String value() default "";
}
