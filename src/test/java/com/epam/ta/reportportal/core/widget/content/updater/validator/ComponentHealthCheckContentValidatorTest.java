package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.MIN_PASSING_RATE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class ComponentHealthCheckContentValidatorTest {

	private MultilevelValidatorStrategy сomponentHealthCheckContentValidator;

	@BeforeEach
	public void setUp() {
		сomponentHealthCheckContentValidator = new ComponentHealthCheckContentValidator();
	}

	@Test
	public void testValidateWithException() {
		WidgetOptions widgetOptions = new WidgetOptions(getWidgetOptionsContentWithBlankKey());
		Exception exception = assertThrows(ReportPortalException.class,
				() -> сomponentHealthCheckContentValidator.validate(Collections.singletonList("test"),
						new HashMap<>(),
						widgetOptions,
						new String[] { "v1" },
						new HashMap<>(),
						100
				)
		);

		String expectedMessage = "Current level key should be not blank";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));

		WidgetOptions wo = new WidgetOptions(getWidgetOptionsContent());
		сomponentHealthCheckContentValidator.validate(Collections.singletonList("test"),
				new HashMap<>(),
				wo,
				new String[] { "v1" },
				new HashMap<>(),
				100
		);
	}

	private Map<String, Object> getWidgetOptionsContent() {
		Map<String, Object> content = new HashMap<>();

		content.put(ATTRIBUTE_KEYS, Lists.newArrayList("k1", "k2"));
		content.put(MIN_PASSING_RATE, 50);

		return content;

	}

	private Map<String, Object> getWidgetOptionsContentWithBlankKey() {
		Map<String, Object> content = new HashMap<>();

		content.put(ATTRIBUTE_KEYS, Lists.newArrayList("k1", ""));
		content.put(MIN_PASSING_RATE, 50);

		return content;

	}
}