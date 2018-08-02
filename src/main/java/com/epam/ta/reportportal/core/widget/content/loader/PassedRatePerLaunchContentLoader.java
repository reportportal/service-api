package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.WidgetContentUtils;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.entity.widget.content.PassStatisticsResult;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budayeu
 */
@Service
public class PassedRatePerLaunchContentLoader implements LoadContentStrategy {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Set<WidgetOption> widgetOptions, int limit) {

		Map<String, List<String>> fields = GROUP_CONTENT_FIELDS.apply(contentFields);
		validateContentFields(fields);

		Map<String, List<String>> options = WidgetContentUtils.GROUP_WIDGET_OPTIONS.apply(widgetOptions);
		validateWidgetOptions(options);

		String launchName = options.get(LAUNCH_NAME_FIELD).iterator().next();

		PassStatisticsResult content = widgetContentRepository.launchPassPerLaunchStatistics(
				filter,
				fields,
				launchRepository.findLatestByNameAndFilter(launchName, filter)
						.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "No launch with name: " + launchName)),
				limit
		);
		return singletonMap(RESULT, content);
	}

	/**
	 * Validate provided widget options. For current widget should be specified launch name.
	 *
	 * @param widgetOptions Set of stored widget options.
	 */
	private void validateWidgetOptions(Map<String, List<String>> widgetOptions) {
		BusinessRule.expect(widgetOptions, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Widget options should not be null.");
		BusinessRule.expect(widgetOptions.containsKey(LAUNCH_NAME_FIELD), Predicate.isEqual(true))
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, LAUNCH_NAME_FIELD + " should be specified for widget.");
	}

	/**
	 * Validate provided content fields.
	 *
	 * @param contentFields Map of provided content.
	 */
	private void validateContentFields(Map<String, List<String>> contentFields) {
		BusinessRule.expect(contentFields, notNull()).verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be null.");
	}
}
