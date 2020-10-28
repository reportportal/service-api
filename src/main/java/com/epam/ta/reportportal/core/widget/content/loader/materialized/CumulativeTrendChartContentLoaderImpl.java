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

package com.epam.ta.reportportal.core.widget.content.loader.materialized;

import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.content.CumulativeTrendChartEntry;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTES;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class CumulativeTrendChartContentLoaderImpl implements MaterializedWidgetContentLoader {

	@Autowired
	private WidgetContentRepository widgetContentRepository;

	@Override
	public Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params) {
		List<CumulativeTrendChartEntry> content;
		List<String> storedAttributes = WidgetOptionUtil.getListByKey(ATTRIBUTES, widget.getWidgetOptions());
		List<String> providedAttributes = ofNullable(params.get(ATTRIBUTES)).map(attributes -> attributes.stream()
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toList())).orElseGet(Collections::emptyList);
		if (providedAttributes.isEmpty()) {
			content = widgetContentRepository.cumulativeTrendChart(
					(String) widget.getWidgetOptions().getOptions().get(VIEW_NAME),
					storedAttributes.get(0),
					storedAttributes.size() > 1 ? storedAttributes.get(1) : null,
					null
			);
		} else {
			verifyProvidedAttributes(storedAttributes, providedAttributes);
			content = widgetContentRepository.cumulativeTrendChart(
					(String) widget.getWidgetOptions().getOptions().get(VIEW_NAME),
					storedAttributes.get(1),
					null,
					providedAttributes.get(0)
			);
		}

		return ImmutableMap.<String, Object>builder().put(RESULT, content).build();
	}

	private void verifyProvidedAttributes(List<String> storedKeys, List<String> providedAttributes) {
		String[] split = providedAttributes.get(0).split(KEY_VALUE_SEPARATOR);
		expect(split.length, Predicate.isEqual(2)).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTES);
		expect(storedKeys.contains(split[0]), Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTES);
		expect(StringUtils.isNoneEmpty(split[1]), Predicate.isEqual(true)).verify(ErrorType.BAD_REQUEST_ERROR, ATTRIBUTES);
	}
}
