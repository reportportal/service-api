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
class UniqueBugContentValidatorTest {

	private WidgetValidatorStrategy uniqueBugContentValidator;

	@BeforeEach
	public void setUp() {
		uniqueBugContentValidator = new UniqueBugContentValidator();
	}

	@Test
	public void testValidateWithException() {
		Exception exception = assertThrows(ReportPortalException.class, () -> {
			uniqueBugContentValidator.validate(Collections.singletonList("test"), new HashMap<>(), new WidgetOptions(), 5);
		});

		String expectedMessage = "Filter-Sort mapping should not be empty";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
}