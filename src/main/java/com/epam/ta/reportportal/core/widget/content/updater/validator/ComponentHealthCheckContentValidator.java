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

package com.epam.ta.reportportal.core.widget.content.updater.validator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTE_KEYS;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.MIN_PASSING_RATE;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ComponentHealthCheckContentValidator implements MultilevelValidatorStrategy {

	public static final Integer MAX_LEVEL_NUMBER = 10;

	@Override
	public void validate(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions, String[] attributes,
			Map<String, String> params, int limit) {

		validateWidgetOptions(widgetOptions);

		List<String> attributeKeys = WidgetOptionUtil.getListByKey(ATTRIBUTE_KEYS, widgetOptions);
		validateAttributeKeys(attributeKeys);

		List<String> attributeValues = ofNullable(attributes).map(Arrays::asList).orElseGet(Collections::emptyList);

		validateAttributeValues(attributeValues);
	}

	private void validateAttributeKeys(List<String> attributeKeys) {
		BusinessRule.expect(attributeKeys, CollectionUtils::isNotEmpty)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "No keys were specified");
		BusinessRule.expect(attributeKeys, cf -> cf.size() <= MAX_LEVEL_NUMBER)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Keys number is incorrect. Maximum keys count = " + MAX_LEVEL_NUMBER);
		attributeKeys.forEach(cf -> BusinessRule.expect(cf, StringUtils::isNotBlank)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Current level key should be not blank"));
	}

	private void validateWidgetOptions(WidgetOptions widgetOptions) {
		BusinessRule.expect(widgetOptions, Objects::nonNull).verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT, "Widgets options not provided");
		Integer passingRate = WidgetOptionUtil.getIntegerByKey(MIN_PASSING_RATE, widgetOptions)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
						"Minimum passing rate option was not specified"
				));
		BusinessRule.expect(passingRate, v -> v >= 0 && v <= 100)
				.verify(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
						"Minimum passing rate value should be greater or equal to 0 and less or equal to 100"
				);
	}

	private void validateAttributeValues(List<String> attributeValues) {
		attributeValues.forEach(value -> BusinessRule.expect(value, Objects::nonNull)
				.verify(ErrorType.BAD_REQUEST_ERROR, "Attribute value should be not null"));
	}
}