package com.epam.ta.reportportal.core.widget.content.loader.util.healthcheck;

import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.MIN_PASSING_RATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class AbstractHealthCheckTableContentResolver implements HealthCheckTableContentResolver {

	public static final Integer MAX_LEVEL_NUMBER = 10;


	protected abstract Map<String, Object> getContent(Widget widget, List<String> attributeKeys, List<String> attributeValues);

	@Override
	public Map<String, Object> loadContent(Widget widget, String[] attributes, Map<String, String> params) {

		WidgetOptions widgetOptions = widget.getWidgetOptions();
		validateWidgetOptions(widgetOptions);

		List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widgetOptions);
		validateAttributeKeys(attributeKeys);

		List<String> attributeValues = ofNullable(attributes).map(Arrays::asList).orElseGet(Collections::emptyList);
		validateAttributeValues(attributeValues);

		return getContent(widget, attributeKeys, attributeValues);

	}

	private void validateAttributeKeys(List<String> attributeKeys) {
		BusinessRule.expect(attributeKeys, CollectionUtils::isNotEmpty)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "No keys were specified");
		BusinessRule.expect(attributeKeys, cf -> cf.size() <= MAX_LEVEL_NUMBER)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Keys number is incorrect. Maximum keys count = " + MAX_LEVEL_NUMBER);
		attributeKeys.forEach(cf -> BusinessRule.expect(cf, StringUtils::isNotBlank)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Current level key should be not null"));
	}

	private void validateWidgetOptions(WidgetOptions widgetOptions) {
		BusinessRule.expect(widgetOptions, Objects::nonNull).verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Widgets options not provided");
		WidgetOptionUtil.getIntegerByKey(MIN_PASSING_RATE, widgetOptions)
				.map(value -> {
					BusinessRule.expect(value, v -> v >= 0 && v <= 100)
							.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
									"Minimum passing rate value should be greater or equal to 0 and less or equal to 100"
							);
					return value;
				})
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
						"Minimum passing rate option was not specified"
				));
	}

	private void validateAttributeValues(List<String> attributeValues) {
		attributeValues.forEach(value -> BusinessRule.expect(value, Objects::nonNull)
				.verify(ErrorType.BAD_REQUEST_ERROR, "Attribute value should be not null"));
	}

}
