package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class NotPassedTestsContentValidatorTest {

	private WidgetValidatorStrategy notPassedTestsContentValidator;

	@BeforeEach
	public void setUp() {
		notPassedTestsContentValidator = new NotPassedTestsContentValidator();
	}

	@Test
	public void testValidateWithException() {
		Exception exception = assertThrows(ReportPortalException.class, () -> {
			notPassedTestsContentValidator.validate(Collections.singletonList("test"), new HashMap<>(), new WidgetOptions(), 5);
		});

		String expectedMessage = "Filter-Sort mapping should not be empty";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
}