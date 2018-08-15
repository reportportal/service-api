package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.ContentField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budayeu
 */
@Service
public class FlakyCasesTableContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetRepository;

	@Override
	public Map<String, ?> loadContent(Set<ContentField> contentFields, Filter filter, Map<String, String> widgetOptions, int limit) {

		return singletonMap(RESULT, widgetRepository.flakyCasesStatistics(filter, limit));
	}
}
