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

import com.epam.ta.reportportal.core.widget.impl.WidgetUtils;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.widget.ContentOptions;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.database.search.FilterConditionUtils;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of
 * {@link com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy} for
 * statistic based widget
 *
 * @author Dzmitry_Kavalets
 */
@Service
public class GeneralFilterStrategy implements BuildFilterStrategyLatest {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private WidgetContentProvider widgetContentProvider;

	private final FilterCondition modeCondition;

	public GeneralFilterStrategy() {
		modeCondition = new FilterCondition(Condition.EQUALS, false, Mode.DEFAULT.toString(), Launch.MODE_CRITERIA);
	}

	@Override
	public Map<String, ?> buildFilterAndLoadContent(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		if (userFilter == null) {
			return Collections.emptyMap();
		}
		Filter searchFilter = userFilter.getFilter();
		if (searchFilter.getTarget().getSimpleName().equalsIgnoreCase(TestItem.class.getSimpleName())) {
			if (null != contentOptions.getMetadataFields() && contentOptions.getMetadataFields().contains(WidgetUtils.NUMBER)) {
				contentOptions.getMetadataFields().remove(WidgetUtils.NUMBER);
			}
		}
		// all widget content should be selected per project, so added
		// additional condition to filter
		searchFilter.addConditions(getAdditionalConditions(searchFilter.getTarget(), projectName));
		return widgetContentProvider.getChartContent(projectName, searchFilter, userFilter.getSelectionOptions(), contentOptions);
	}

	@Override
	public Map<String, List<ChartObject>> loadContentOfLatest(UserFilter userFilter, ContentOptions contentOptions, String projectName) {
		if (userFilter == null) {
			return Collections.emptyMap();
		}
		Filter filter = userFilter.getFilter();
		if (filter.getTarget().equals(Launch.class)) {
			filter.addCondition(new FilterCondition(Condition.EQUALS, false, Mode.DEFAULT.name(), Launch.MODE_CRITERIA));
			filter.addCondition(new FilterCondition(Condition.NOT_EQUALS, false, Status.IN_PROGRESS.name(), Launch.STATUS));
			filter.addCondition(new FilterCondition(Condition.EQUALS, false, projectName, Launch.PROJECT));
		}
		return widgetContentProvider.getChartContent(projectName, userFilter.getFilter(), userFilter.getSelectionOptions(), contentOptions);
	}

	/**
	 * Get {@link com.epam.ta.reportportal.database.search.FilterCondition}s for
	 * selecting data foe widget Additional conditions:
	 * <li>data only from current project;
	 * <li>data only from non debug launches;
	 *
	 * @param type
	 * @param projectName
	 * @return FilterCondition
	 */
	private Set<FilterCondition> getAdditionalConditions(Class<?> type, String projectName) {
		Set<FilterCondition> result = new HashSet<>();
		// TODO consider to avoid this(if operations)
		if (TestItem.class.equals(type)) {
			result.add(getConditionForTestItem(projectName));
		} else if (type.equals(Launch.class)) {
			result.add(new FilterCondition(Condition.EQUALS, false, projectName, Launch.PROJECT));
			// skip in progress launches
			result.add(FilterConditionUtils.LAUNCH_NOT_IN_PROGRESS());
			result.add(modeCondition);
		} else {
			throw new ReportPortalException("Unable load content per field");
		}
		return result;
	}

	/**
	 * {@link TestItem} doen't has
	 * {@link com.epam.ta.reportportal.database.entity.Project} field, in this
	 * case<br>
	 * we should find all launches by project and mode(not Debug mode) and use
	 * them for searching<br>
	 * testItem using "In" filter.
	 *
	 * @param projectName
	 * @return FilterCondition
	 */
	private FilterCondition getConditionForTestItem(String projectName) {
		Filter filter = new Filter(Launch.class, Condition.EQUALS, false, projectName, Launch.PROJECT);
		filter.addCondition(modeCondition);
		List<Launch> launches = launchRepository.findIdsByFilter(filter);
		final String value = launches.stream().map(Launch::getId).collect(Collectors.joining(Condition.VALUES_SEPARATOR));
		return new FilterCondition(Condition.IN, false, value, TestItem.LAUNCH_CRITERIA);
	}
}