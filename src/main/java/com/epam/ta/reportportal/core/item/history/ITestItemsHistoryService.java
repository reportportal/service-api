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
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;

import java.util.List;

/**
 * Define interface for loading and validating additional data( launches and
 * test Items) which should be used for loading test items history.
 *
 * @author Aliaksei_Makayed
 */
public interface ITestItemsHistoryService {

	/**
	 * Load launches for which history should be loaded
	 *
	 * @param quantity           - count items in history
	 * @param startingLaunchId   - first initial launch in history
	 * @param projectName        - name of project
	 * @param showBrokenLaunches - <b>boolean</b> should in_progress and interrupted launches
	 *                           been included in history:<br>
	 *                           <code>true</code> - if history should contain all launch
	 *                           statuses<br>
	 *                           <code>false</code> - if history should contain only passed and
	 *                           failed launches
	 * @return
	 */
	List<Launch> loadLaunches(int quantity, String startingLaunchId, String projectName, boolean showBrokenLaunches);

	/**
	 * Build ui representation of launch history
	 *
	 * @param launch    History launch
	 * @param testItems History test items
	 * @return {@link TestItemHistoryElement}
	 */
	TestItemHistoryElement buildHistoryElement(Launch launch, List<TestItem> testItems);

	void validateHistoryRequest(String projectName, String[] startPointsIds, int historyDepth);

	void validateItems(List<TestItem> itemsForHistory, List<String> ids, String projectName);

}