package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonMap;

@Service
public class ProductStatusFilterGroupedContentLoader implements ProductStatusContentLoader {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Set<Filter> filters, Sort sort, Map<String, String> widgetOptions,
			int limit) {
		boolean latestMode = widgetOptions.entrySet().stream().anyMatch(entry -> LATEST_OPTION.equalsIgnoreCase(entry.getKey()));

		return singletonMap(RESULT,
				widgetContentRepository.productStatusGroupedByFilterStatistics(filters, contentFields, sort, latestMode, limit)
		);
	}
}
