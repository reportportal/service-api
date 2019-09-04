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

package com.epam.ta.reportportal.core.item.history;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.ws.model.TestItemHistoryElement;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Define handler's operations for loading testItem's history.
 *
 * @author Aliaksei_Makayed
 */
public interface TestItemsHistoryHandler {

	/**
	 * Get history for {@link TestItem}s according to input parameters
	 *
	 * @param projectDetails - project details
	 * @param user - current user
	 * @param filter - filter
	 * @param pageable - paging parameters object
	 * @param launchId - id of a launch to start history from
	 * @param filterId - filter id
	 * @param launchesLimit - launches limit
	 * @param historyDepth - count of items in history
	 * @return List of {@link TestItemHistoryElement}
	 */
	List<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, Queryable filter,
			Pageable pageable, @Nullable Long launchId, @Nullable Long filterId, int launchesLimit, int historyDepth);
}