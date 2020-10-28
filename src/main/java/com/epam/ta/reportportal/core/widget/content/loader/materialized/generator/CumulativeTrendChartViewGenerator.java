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

package com.epam.ta.reportportal.core.widget.content.loader.materialized.generator;

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.widget.util.WidgetOptionUtil;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.widget.Widget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.ATTRIBUTES;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class CumulativeTrendChartViewGenerator extends AbstractViewGenerator {

	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public CumulativeTrendChartViewGenerator(WidgetRepository widgetRepository, WidgetContentRepository widgetContentRepository) {
		super(widgetRepository);
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	protected void generateView(boolean refresh, String viewName, Widget widget, Filter launchesFilter, Sort launchesSort,
			MultiValueMap<String, String> params) {
		List<String> attributes = WidgetOptionUtil.getListByKey(ATTRIBUTES, widget.getWidgetOptions());
		widgetContentRepository.generateCumulativeTrendChartView(refresh, viewName, launchesFilter, launchesSort, attributes, widget.getItemsCount());
	}
}
