/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.widget.content.BuildFilterStrategy;
import com.epam.ta.reportportal.core.widget.content.LoadContentStrategy;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
@Service
public class GeneralStatisticsFilterStrategy implements BuildFilterStrategy {

	@Override
	public Map<String, ?> buildFilterAndLoadContent(LoadContentStrategy loadContentStrategy, ReportPortalUser.ProjectDetails projectDetails,
			Widget widget) {
		Set<Filter> filters = widget.getFilters().stream().map(it -> {
			try {
				return new Filter(Class.forName(it.getTargetClass()), it.getFilterCondition());
			} catch (ClassNotFoundException e) {
				throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, e.getMessage());
			}
		}).collect(Collectors.toSet());

		getDefaultFilter(Launch.class, projectDetails.getProjectId());
		return loadContentStrategy.loadContent(widget.getContentFields(), filters, widget.getWidgetOptions());
	}

	private Filter getDefaultFilter(Class target, Long projectId) {
		Set<FilterCondition> basicConditions = new HashSet<>();
		basicConditions.add(new FilterCondition(Condition.EQUALS, false, String.valueOf(projectId), "l.project_id"));
		basicConditions.add(new FilterCondition(Condition.NOT_EQUALS, false, StatusEnum.IN_PROGRESS.name(), "l.status"));
		basicConditions.add(new FilterCondition(Condition.EQUALS, false, Mode.DEFAULT.toString(), "l.mode"));
		return new Filter(target, basicConditions);
	}
}
