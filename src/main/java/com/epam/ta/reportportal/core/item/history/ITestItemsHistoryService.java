/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.item.history;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
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
	List<Launch> loadLaunches(int quantity, Long startingLaunchId, String projectName, boolean showBrokenLaunches);

	/**
	 * Build ui representation of launch history
	 *
	 * @param launch    History launch
	 * @param testItems History test items
	 * @return {@link TestItemHistoryElement}
	 */
	TestItemHistoryElement buildHistoryElement(Launch launch, List<TestItem> testItems);

	void validateHistoryRequest(String projectName, Long[] startPointsIds, int historyDepth);

	void validateItems(List<TestItem> itemsForHistory, List<String> ids, String projectName);

}