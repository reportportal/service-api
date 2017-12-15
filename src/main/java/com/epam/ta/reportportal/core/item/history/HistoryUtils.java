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

package com.epam.ta.reportportal.core.item.history;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.search.Condition;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.database.search.FilterConditionUtils;

/**
 * Provide utilities for loading test items history
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
class HistoryUtils {

	private HistoryUtils() {
	}

	// only launches with passed or failed status should be included in history
	public static final FilterCondition STATUS_CONDITION = new FilterCondition(Condition.IN, false,
			new StringBuilder(Status.PASSED.toString()).append(Condition.VALUES_SEPARATOR)
					.append(Status.FAILED.toString())
					.append(Condition.VALUES_SEPARATOR)
					.append(Status.STOPPED.toString())
					.toString(), "status"
	);

	/**
	 * Create launches filter which can be used during selection launches for
	 * history.
	 *
	 * @param launchName         - name of searching launch
	 * @param projectName        - name of project
	 * @param number		 	 - the starting launch number
	 * @param showBrokenLaunches - <b>boolean</b> should in_progress and interrupted launches
	 *                           been included in history:<br>
	 *                           <code>true</code> - if history should contain all launch
	 *                           statuses<br>
	 *                           <code>false</code> - if history should contain only passed and
	 *                           failed launches
	 * @return Filter
	 */
	public static Filter getLaunchSelectionFilter(String launchName, String projectName, String number, boolean showBrokenLaunches) {

		Filter filter = new Filter(Launch.class, Condition.EQUALS, false, launchName, "name");
		FilterCondition projectCondition = new FilterCondition(Condition.EQUALS, false, projectName, "project");
		FilterCondition startTimeCondition = new FilterCondition(Condition.LOWER_THAN_OR_EQUALS, false, number, "number");

		filter.addCondition(projectCondition);
		filter.addCondition(startTimeCondition);
		filter.addCondition(FilterConditionUtils.LAUNCH_IN_DEFAULT_MODE());
		if (!showBrokenLaunches) {
			filter.addCondition(STATUS_CONDITION);
		}

		return filter;
	}
}