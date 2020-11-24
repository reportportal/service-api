/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.widget.content.loader;

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.FlakyCasesTableContent;
import com.epam.ta.reportportal.entity.widget.content.LatestLaunchContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.*;
import static com.epam.ta.reportportal.core.widget.util.WidgetFilterUtil.GROUP_FILTERS;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class FlakyCasesTableContentLoader implements LoadContentStrategy {

	@Autowired
	private WidgetContentRepository widgetRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		String launchName = WidgetOptionUtil.getValueByKey(LAUNCH_NAME_FIELD, widgetOptions);
		filter.withCondition(new FilterCondition(Condition.EQUALS, false, launchName, CRITERIA_NAME));

		Launch launch = launchRepository.findLatestByFilter(filter)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, "No launch with name: " + launchName));
		LatestLaunchContent latestLaunchContent = new LatestLaunchContent(launch);

		List<FlakyCasesTableContent> flakyCasesTableContent = widgetRepository.flakyCasesStatistics(filter,
				ofNullable(widgetOptions.getOptions().get(INCLUDE_METHODS)).map(v -> BooleanUtils.toBoolean(String.valueOf(v)))
						.orElse(false),
				limit
		);
		return CollectionUtils.isEmpty(flakyCasesTableContent) ?
				emptyMap() :
				ImmutableMap.<String, Object>builder().put(LATEST_LAUNCH, latestLaunchContent).put(FLAKY, flakyCasesTableContent).build();
	}

}
