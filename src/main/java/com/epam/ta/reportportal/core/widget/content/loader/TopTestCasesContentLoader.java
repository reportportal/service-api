/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.content.CriteraHistoryItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;

/**
 * Content loader for {@link com.epam.ta.reportportal.entity.widget.WidgetType#TOP_TEST_CASES}
 *
 * @author Pavel Bortnik
 */
@Service
public class TopTestCasesContentLoader implements LoadContentStrategy {

	private LaunchRepository launchRepository;

	private WidgetContentRepository widgetContentRepository;

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Autowired
	public void setWidgetContentRepository(WidgetContentRepository widgetContentRepository) {
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, Map<String, String> widgetOptions,
			int limit) {
		String contentField = validateContentFields(contentFields);
		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		Launch latestByName = launchRepository.findLatestByNameAndFilter(widgetOptions.get(LAUNCH_NAME_FIELD), filter)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, widgetOptions.get(LAUNCH_NAME_FIELD)));
		List<CriteraHistoryItem> content = widgetContentRepository.topItemsByCriteria(filter,
				contentField,
				limit,
				BooleanUtils.toBoolean(widgetOptions.get(INCLUDE_METHODS))
		);
		return ImmutableMap.<String, Object>builder().put(LATEST_LAUNCH, LaunchConverter.TO_RESOURCE.apply(latestByName))
				.put(RESULT, content)
				.build();
	}

	/**
	 * Validate provided content fields. For current widget it should be only one field specified in content fields.
	 * Example is 'executions$failed', so widget would be created by 'failed' criteria.
	 *
	 * @param contentFields List of provided content.
	 */
	private String validateContentFields(List<String> contentFields) {
		BusinessRule.expect(CollectionUtils.isNotEmpty(contentFields), equalTo(true))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Content fields should not be empty");
		BusinessRule.expect(contentFields.size(), Predicate.isEqual(1))
				.verify(ErrorType.BAD_REQUEST_ERROR, "Only one content field could be specified.");
		return contentFields.get(0);
	}
}
