package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.TopPatternTemplatesContent;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TopPatternContentLoader implements MultilevelLoadContentStrategy {

	public static final Integer TOP_PATTERN_TEMPLATES_ATTRIBUTES_COUNT = 15;

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			String[] attributes, Map<String, String> params, int limit) {

		validateFilterSortMapping(filterSortMapping);
		validateWidgetOptions(widgetOptions);

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		List<TopPatternTemplatesContent> content = widgetContentRepository.patternTemplate(filter,
				sort,
				WidgetOptionUtil.getValueByKey(ATTRIBUTE_KEY, widgetOptions),
				params.get(PATTERN_TEMPLATE_NAME),
				WidgetOptionUtil.getBooleanByKey(LATEST_OPTION, widgetOptions),
				TOP_PATTERN_TEMPLATES_ATTRIBUTES_COUNT
		);

		return content.isEmpty() ? Collections.emptyMap() : Collections.singletonMap(RESULT, content);
	}

	/**
	 * Mapping should not be empty
	 *
	 * @param filterSortMapping Map of ${@link Filter} for query building as key and ${@link Sort} as value for each filter
	 */
	private void validateFilterSortMapping(Map<Filter, Sort> filterSortMapping) {
		expect(MapUtils.isNotEmpty(filterSortMapping), equalTo(true)).verify(ErrorType.BAD_REQUEST_ERROR,
				"Filter-Sort mapping should not be empty"
		);
	}

	/**
	 * Validate provided widget options. For current widget launch name should be specified.
	 *
	 * @param widgetOptions Map of stored widget options.
	 */
	private void validateWidgetOptions(WidgetOptions widgetOptions) {
		BusinessRule.expect(WidgetOptionUtil.getValueByKey(ATTRIBUTE_KEY, widgetOptions), StringUtils::isNotEmpty)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, ATTRIBUTE_KEY + " should be specified for widget.");
	}
}
