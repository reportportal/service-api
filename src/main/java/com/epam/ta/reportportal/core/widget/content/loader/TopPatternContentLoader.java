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

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.MultilevelLoadContentStrategy;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.entity.widget.content.TopPatternTemplatesContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	public Map<String, Object> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			String[] attributes, MultiValueMap<String, String> params, int limit) {

		Filter filter = GROUP_FILTERS.apply(filterSortMapping.keySet());
		Sort sort = GROUP_SORTS.apply(filterSortMapping.values());

		List<TopPatternTemplatesContent> content = widgetContentRepository.patternTemplate(filter,
				sort,
				WidgetOptionUtil.getValueByKey(ATTRIBUTE_KEY, widgetOptions),
				params.getFirst(PATTERN_TEMPLATE_NAME),
				WidgetOptionUtil.getBooleanByKey(LATEST_OPTION, widgetOptions),
				limit,
				TOP_PATTERN_TEMPLATES_ATTRIBUTES_COUNT
		);

		return content.isEmpty() ? Collections.emptyMap() : Collections.singletonMap(RESULT, content);
	}

}
