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
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import org.springframework.data.domain.Pageable;

/**
 * Define handler's operations for loading testItem's history.
 *
 * @author Aliaksei_Makayed
 */
public interface TestItemsHistoryHandler {

	/**
	 * Get history for {@link com.epam.ta.reportportal.entity.item.TestItem}s according to input parameters
	 *
	 * @param projectDetails       - project details
	 * @param filter               - filter
	 * @param pageable             - paging parameters object
	 * @param historyRequestParams - {@link HistoryRequestParams}
	 * @param user                 - {@link ReportPortalUser}
	 * @return {@link Iterable} of {@link TestItemHistoryElement}
	 */
	Iterable<TestItemHistoryElement> getItemsHistory(ReportPortalUser.ProjectDetails projectDetails, Queryable filter, Pageable pageable,
			HistoryRequestParams historyRequestParams, ReportPortalUser user);

}