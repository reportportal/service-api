package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class MostTimeConsumingContentValidatorTest {

	private WidgetValidatorStrategy mostTimeConsumingContentValidator;

	@BeforeEach
	public void setUp() {
		mostTimeConsumingContentValidator = new MostTimeConsumingContentValidator();
	}

	@Test
	public void testValidateWithException() {
		Exception exception = assertThrows(ReportPortalException.class, () -> {
			HashMap<Filter, Sort> filterSortMap = new HashMap<>();
			filterSortMap.put(Filter.builder()
					.withTarget(Launch.class)
					.withCondition(FilterCondition.builder().eq("id", "1").build())
					.build(), Sort.unsorted());
			Map<String, Object> params = new HashMap<>();
			params.put(LAUNCH_NAME_FIELD, "");
			WidgetOptions widgetOptions = new WidgetOptions();
			mostTimeConsumingContentValidator.validate(Collections.singletonList("test"), filterSortMap,
					widgetOptions, 5);
		});

		String expectedMessage = LAUNCH_NAME_FIELD + " should be specified for widget.";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}

}