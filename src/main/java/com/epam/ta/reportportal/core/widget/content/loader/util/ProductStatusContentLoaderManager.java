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

package com.epam.ta.reportportal.core.widget.content.loader.util;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.core.widget.content.loader.ProductStatusContentLoader;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.entity.widget.WidgetOptions;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ProductStatusContentLoaderManager implements LoadContentStrategy {

	private final Map<String, ProductStatusContentLoader> productStatusContentLoader;

	@Autowired
	public ProductStatusContentLoaderManager(
			@Qualifier("productStatusContentLoader") Map<String, ProductStatusContentLoader> productStatusContentLoader) {
		this.productStatusContentLoader = productStatusContentLoader;
	}

	@Override
	public Map<String, ?> loadContent(List<String> contentFields, Map<Filter, Sort> filterSortMapping, WidgetOptions widgetOptions,
			int limit) {

		String strategy = WidgetOptionUtil.getValueByKey("strategy", widgetOptions);

		return ofNullable(productStatusContentLoader.get(strategy)).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
				"Wrong strategy type. Allowed: \"filter\", \"launch\". But found: " + strategy
		))
				.loadContent(contentFields, filterSortMapping, widgetOptions, limit);
	}
}
