package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlakyCasesTableContentValidatorTest {
	private WidgetValidatorStrategy flakyCasesTableContentValidator;

	@BeforeEach
	public void setUp() {
		flakyCasesTableContentValidator = new FlakyCasesTableContentValidator();
	}

	@Test
	public void testValidateWithException() {
		Exception exception = assertThrows(ReportPortalException.class, () -> {
			flakyCasesTableContentValidator.validate(null, new HashMap<>(), new WidgetOptions(), 5);
		});

		String expectedMessage = "Unable to load widget content. Widget proprties contains errors: launchNameFilter should be specified for widget.";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

}
