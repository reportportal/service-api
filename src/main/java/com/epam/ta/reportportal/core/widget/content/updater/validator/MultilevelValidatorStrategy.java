package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public interface MultilevelValidatorStrategy {
	void validate(List<String> contentFields, Map<Filter, Sort> filterSortMap, WidgetOptions widgetOptions,
			String[] attributes, Map<String, String> params, int limit);
}
