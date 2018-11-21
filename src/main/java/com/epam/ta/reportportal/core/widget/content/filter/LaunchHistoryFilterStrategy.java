package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.LAUNCH_NAME_FIELD;

/**
 * @author Pavel Bortnik
 */
@Service("launchHistoryFilterStrategy")
public class LaunchHistoryFilterStrategy extends GeneralLaunchFilterStrategy {

	@Override
	protected Filter buildDefaultFilter(Widget widget, Long projectId) {
		validateWidgetOptions(widget.getWidgetOptions());
		String launchName = WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widget.getWidgetOptions());
		Filter filter = super.buildDefaultFilter(widget, projectId);
		return filter.withCondition(new FilterCondition(Condition.EQUALS, false, launchName, CRITERIA_NAME));
	}

	/**
	 * Validate provided widget options. For current widget launch name should be specified.
	 *
	 * @param widgetOptions Map of stored widget options.
	 */
	private void validateWidgetOptions(WidgetOptions widgetOptions) {
		BusinessRule.expect(WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions), StringUtils::isNotBlank)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");
	}
}
