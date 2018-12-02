package com.epam.ta.reportportal.core.widget.content.loader.util;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.entity.launch.Launch;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class FilterUtils {

	private FilterUtils() {
		//static only
	}

	public static Filter buildLatestLaunchFilter(Long projectId, String launchName) {
		return Filter.builder()
				.withTarget(Launch.class)
				.withCondition(new FilterCondition(Condition.EQUALS, false, String.valueOf(projectId), CRITERIA_PROJECT_ID))
				.withCondition(new FilterCondition(Condition.EQUALS, false, launchName, CRITERIA_NAME))
				.build();
	}
}
