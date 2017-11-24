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

import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.filter.SelectionOrder;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of
 * {@link com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy} for
 * activity stream widget
 *
 * @author Dzmitry_Kavalets
 */
@Service("ActivityFilterStrategy")
public class ActivityFilterStrategy implements BuildFilterStrategy {

	public static final Integer QUANTITY = 50;
	private static final Integer PAGE_NUMBER = 1;
	private static final String LAST_MODIFIED = "last_modified";
	private static final String SEPARATOR = ",";
	private static final String[] options = { Activity.ACTION_TYPE, Activity.USER_REF, Activity.OBJECT_TYPE };

	@Autowired
	private WidgetContentProvider widgetContentProvider;

	@Override
	public Map<String, List<ChartObject>> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions,
			String projectName) {

		Filter searchFilter = new Filter(Activity.class, Condition.EQUALS, false, projectName, Activity.PROJECT_REF);

		if (null != contentOptions.getWidgetOptions()) {

			Map<String, List<String>> widgetOptions = contentOptions.getWidgetOptions();
			for (String option : options) {
				if (widgetOptions.containsKey(option)) {
					searchFilter.addCondition(buildFilterCondition(option, widgetOptions.get(option)));
				}
			}
		}

		SelectionOptions selectionOptions = new SelectionOptions();
		SelectionOrder selectionOrder = new SelectionOrder();
		selectionOrder.setIsAsc(false);
		selectionOrder.setSortingColumnName(LAST_MODIFIED);
		selectionOptions.setPageNumber(PAGE_NUMBER);
		selectionOptions.setOrders(Collections.singletonList(selectionOrder));
		return widgetContentProvider.getChartContent(projectName, searchFilter, selectionOptions, contentOptions);
	}

	private FilterCondition buildFilterCondition(String searchCriteria, List<String> values) {
		Condition condition;
		if (null != values && values.size() > 1) {
			condition = Condition.IN;
		} else {
			condition = Condition.EQUALS;
		}
		return new FilterCondition(condition, false, StringUtils.join(values, SEPARATOR), searchCriteria);
	}
}