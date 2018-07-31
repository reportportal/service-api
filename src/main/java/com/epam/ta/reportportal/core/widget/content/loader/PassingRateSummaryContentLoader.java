package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOption;
import com.epam.ta.reportportal.entity.widget.content.PassStatisticsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budayeu
 */
@Service
public class PassingRateSummaryContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Filter filter, Set<WidgetOption> widgetOptions, int limit) {
		PassStatisticsResult result = widgetContentRepository.summaryPassStatistics(filter, GROUP_CONTENT_FIELDS.apply(contentFields));
		return singletonMap(RESULT, result);
	}
}
