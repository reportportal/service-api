package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class LaunchesTableContentValidatorTest {

	private WidgetValidatorStrategy launchesTableContentValidator;

	@BeforeEach
	public void setUp() {
		launchesTableContentValidator = new LaunchesTableContentValidator();
	}

	@Test
	public void testValidateWithException() {
		Exception exception = assertThrows(ReportPortalException.class, () -> {
			HashMap<Filter, Sort> filterSortMap = new HashMap<>();
			filterSortMap.put(Filter.builder()
					.withTarget(Launch.class)
					.withCondition(FilterCondition.builder().eq("id", "1").build())
					.build(), Sort.unsorted());
			launchesTableContentValidator.validate(new ArrayList<>(), filterSortMap, new WidgetOptions(), 5);
		});

		String expectedMessage = "Content fields should not be empty";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
}