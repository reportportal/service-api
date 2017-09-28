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

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;

import java.util.List;

/**
 * Define handler's operations for loading testItem's history.
 *
 * @author Aliaksei_Makayed
 */
public interface TestItemsHistoryHandler {

	/**
	 * Get history for of {@link TestItem}s according input parameters:<br>
	 * <li>isSoloSelection - select history for specified by id item <li>isRoot
	 * - select history for suites of specified by if launch <li>default -
	 * select history for all children of specified by id item
	 *
	 * @param projectName    - project name
	 * @param startPointsIds - ids of history start points(launch id or testitem id)
	 * @param historyDepth   - count of items in history
	 * @return List of {@link TestItemHistoryElement}
	 */
	List<TestItemHistoryElement> getItemsHistory(String projectName, String[] startPointsIds, int historyDepth, boolean showBrokenLaunches);

}