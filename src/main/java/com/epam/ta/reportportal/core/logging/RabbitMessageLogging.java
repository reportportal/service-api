package com.epam.ta.reportportal.core.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Konstantin Antipin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RabbitMessageLogging {

	boolean logHeaders() default true;

	boolean logBody() default true;
}
