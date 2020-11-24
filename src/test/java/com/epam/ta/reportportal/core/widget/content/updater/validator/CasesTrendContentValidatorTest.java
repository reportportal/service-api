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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class CasesTrendContentValidatorTest {

	private WidgetValidatorStrategy casesTrendContentValidator;

	@BeforeEach
	public void setUp() {
		casesTrendContentValidator = new CasesTrendContentValidator();
	}

	@Test
	public void testValidateWithException() {
		Exception exception = assertThrows(ReportPortalException.class, () -> {
			HashMap<Filter, Sort> filterSortMap = new HashMap<>();
			filterSortMap.put(Filter.builder()
					.withTarget(Launch.class)
					.withCondition(FilterCondition.builder().eq("id", "1").build())
					.build(), Sort.unsorted());
			casesTrendContentValidator.validate(Collections.singletonList("test"), filterSortMap, new WidgetOptions(), 5);
		});

		String expectedMessage = "Bad content fields format";
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage));
	}
}