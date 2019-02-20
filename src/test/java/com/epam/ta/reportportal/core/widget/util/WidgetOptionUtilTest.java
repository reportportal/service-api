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

import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class WidgetOptionUtilTest {

	private static final String FIRST_KEY = "KEY1";
	private static final String SECOND_KEY = "KEY2";
	private static final String FIRST_STRING_VALUE = "VALUE1";
	private static final String SECOND_STRING_VALUE = "VALUE2";

	@Test
	void getStringValueWhenCorrectTypeTest() {

		//given
		WidgetOptions widgetOptions = new WidgetOptions(getMapWithStringValues());

		//when
		String value = WidgetOptionUtil.getValueByKey(FIRST_KEY, widgetOptions);

		//then
		assertNotNull(value);
		assertEquals(FIRST_STRING_VALUE, value);
	}

	@Test
	void throwExceptionWhenGetStringValueWithInCorrectTypeTest() {

		//given
		WidgetOptions widgetOptions = new WidgetOptions(getMapWithNonStringValues());

		//when //then throw exception
		assertThrows(ReportPortalException.class, () -> WidgetOptionUtil.getValueByKey(FIRST_KEY, widgetOptions));
	}

	@Test
	void getMapValueWhenCorrectTypeTest() {

		//given
		WidgetOptions widgetOptions = new WidgetOptions(getMapWithNonStringValues());

		//when
		Map<String, String> mapByKey = WidgetOptionUtil.getMapByKey(FIRST_KEY, widgetOptions);

		//then
		assertNotNull(mapByKey);
	}

	@Test
	void throwExceptionWhenGetMapValueWithInCorrectTypeTest() {

		//given
		WidgetOptions widgetOptions = new WidgetOptions(getMapWithStringValues());

		//when //then throw exception
		assertThrows(ReportPortalException.class, () -> WidgetOptionUtil.getMapByKey(FIRST_KEY, widgetOptions));
	}

	private Map<String, Object> getMapWithStringValues() {
		return ImmutableMap.<String, Object>builder().put(FIRST_KEY, FIRST_STRING_VALUE).put(SECOND_KEY, SECOND_STRING_VALUE).build();
	}

	private Map<String, Object> getMapWithNonStringValues() {
		Map<String, Object> mapValue = Maps.newHashMap();
		mapValue.put(FIRST_KEY, ImmutableList.<String>builder().add(FIRST_STRING_VALUE).build());

		return ImmutableMap.<String, Object>builder().put(FIRST_KEY, mapValue).build();
	}
}