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

import com.epam.ta.reportportal.auth.ReportPortalUser;
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
	 * @param projectDetails - project details
	 * @param startPointsIds - ids of history start points(launch id or testitem id)
	 * @param historyDepth   - count of items in history
	 * @return List of {@link TestItemHistoryElement}
	 */
	List<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, Long[] startPointsIds, int historyDepth,
			boolean showBrokenLaunches);

}