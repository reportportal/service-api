/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.util;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class WidgetOptionUtil {

	private WidgetOptionUtil() {
		//static only
	}

	public static String getValueByKey(String key, WidgetOptions widgetOptions) {
		expect(widgetOptions, notNull()).verify(ErrorType.OBJECT_RETRIEVAL_ERROR, "Widget options should be not null.");
		expect(MapUtils.isNotEmpty(widgetOptions.getOptions()), equalTo(true)).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
				"Widget options should be not empty."
		);
		Object value = widgetOptions.getOptions().get(key);
		expect(value, String.class::isInstance).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
				Suppliers.formattedSupplier("Wrong widget option value type for key = '{}'. String expected.", key)
		);
		return (String) value;
	}

	public static Map<String, List<String>> getMapByKey(String key, WidgetOptions widgetOptions) {
		expect(widgetOptions, notNull()).verify(ErrorType.OBJECT_RETRIEVAL_ERROR, "Widget options should be not null.");
		expect(MapUtils.isNotEmpty(widgetOptions.getOptions()), equalTo(true)).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
				"Widget options should be not empty."
		);

		Object value = widgetOptions.getOptions().get(key);
		expect(value, Map.class::isInstance).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
				Suppliers.formattedSupplier("Wrong widget option value type for key = '{}'. Map expected.", key)
		);

		return (Map<String, List<String>>) value;
	}

	public static boolean containsKey(String key, WidgetOptions widgetOptions) {
		expect(widgetOptions, notNull()).verify(ErrorType.OBJECT_RETRIEVAL_ERROR, "Widget options should be not null.");
		expect(MapUtils.isNotEmpty(widgetOptions.getOptions()), equalTo(true)).verify(ErrorType.OBJECT_RETRIEVAL_ERROR,
				"Widget options should be not empty."
		);

		return widgetOptions.getOptions().containsKey(key);
	}
}
