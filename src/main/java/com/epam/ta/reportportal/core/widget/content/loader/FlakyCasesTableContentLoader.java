package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budayeu
 */
@Service
public class FlakyCasesTableContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Sort sort, Map<String, String> widgetOptions, int limit) {

		return singletonMap(RESULT, widgetRepository.flakyCasesStatistics(filter, limit));
	}
}
