/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.widget.content;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Filter strategy for Launches comparison chart & Launches duration widget
 *
 * @author Dzmitry_Kavalets
 */
@Service
public class CompareLaunchesFilterStrategy implements BuildFilterStrategyLatest {

	@Autowired
	private WidgetContentProvider widgetContentProvider;

	@Override
	public Map<String, List<ChartObject>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {
		if (null == userFilter) {
			return emptyMap();
		}
		Filter filter = extendFilter(userFilter.getFilter(), projectName);
		return widgetContentProvider.getChartContent(projectName, filter, userFilter.getSelectionOptions(), contentOptions);
	}

	@Override
	public Map<String, List<ChartObject>> loadContentOfLatest(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		if (null == userFilter) {
			return emptyMap();
		}
		Filter filter = extendFilter(userFilter.getFilter(), projectName);
		return widgetContentProvider.getChartContent(projectName, filter, userFilter.getSelectionOptions(), contentOptions);
	}

	private Filter extendFilter(Filter filter, String projectName) {
		if (filter.getTarget().equals(Launch.class)) {
			filter.addCondition(new FilterCondition(Condition.EQUALS, false, Mode.DEFAULT.name(), Launch.MODE_CRITERIA));
			filter.addCondition(new FilterCondition(Condition.NOT_EQUALS, false, Status.IN_PROGRESS.name(), Launch.STATUS));
			filter.addCondition(new FilterCondition(Condition.EQUALS, false, projectName, Launch.PROJECT));
		}
		return filter;
	}

}