package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.ContentField;
import com.epam.ta.reportportal.entity.widget.content.PassStatisticsResult;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.widget.content.WidgetContentUtils.GROUP_CONTENT_FIELDS;
import static com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants.EXECUTIONS_KEY;
import static java.util.Collections.singletonMap;

/**
 * @author Ivan Budayeu
 */
@Service
public class PassingRateSummaryContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, ?> loadContent(Set<ContentField> contentFields, Filter filter, Map<String, String> widgetOptions, int limit) {

		Map<String, List<String>> fields = GROUP_CONTENT_FIELDS.apply(contentFields);
		validateContentFields(fields);

		PassStatisticsResult result = widgetContentRepository.summaryPassStatistics(filter, fields, limit);
		return singletonMap(RESULT, result);
	}

	/**
	 * Validate provided content fields.
	 * For this widget content field only with {@link com.epam.ta.reportportal.dao.WidgetContentRepositoryConstants#EXECUTIONS_KEY}
	 * key should be specified
	 * <p>
	 * The value of content field should not be empty
	 *
	 * @param contentFields Map of provided content.
	 */
	private void validateContentFields(Map<String, List<String>> contentFields) {
		BusinessRule.expect(MapUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), equalTo(1))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Passing rate summary content fields should contain only one key - " + EXECUTIONS_KEY);
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields.get(EXECUTIONS_KEY)), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "The value of content field - " + EXECUTIONS_KEY + " - should not be empty");

	}
}
