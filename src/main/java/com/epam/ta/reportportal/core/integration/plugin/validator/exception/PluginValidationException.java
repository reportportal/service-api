package com.epam.ta.reportportal.core.integration.plugin.validator.exception;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginValidationException extends Exception {

	public PluginValidationException(String message) {
		super(message);
	}

	public PluginValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
