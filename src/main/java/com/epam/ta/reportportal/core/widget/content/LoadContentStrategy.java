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

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * Strategy definition interface for loading widget content.
 *
 * @author Pavel Bortnik
 */
public interface LoadContentStrategy {

	Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMap, WidgetOptions widgetOptions, int limit);

}