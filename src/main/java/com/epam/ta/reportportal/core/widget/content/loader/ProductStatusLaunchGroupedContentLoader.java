package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_SORTS;
import static java.util.Collections.singletonMap;

@Service
public class ProductStatusLaunchGroupedContentLoader implements ProductStatusContentLoader {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> fields, Map<Filter, Sort> filterSortMapping, Map<String, String> widgetOptions,
			int limit) {

		boolean latestMode = widgetOptions.entrySet().stream().anyMatch(entry -> LATEST_OPTION.equalsIgnoreCase(entry.getKey()));

		List<String> tags = fields.stream()
				.filter(f -> f.startsWith("tag"))
				.map(field -> field.split("\\$")[1])
				.collect(Collectors.toList());

		List<String> contentFields = fields.stream().filter(f -> !f.startsWith("tag")).collect(Collectors.toList());

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());

		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		return singletonMap(RESULT,
				widgetContentRepository.productStatusGroupedByLaunchesStatistics(filter, contentFields, tags, sort, latestMode, limit)
		);
	}

}
