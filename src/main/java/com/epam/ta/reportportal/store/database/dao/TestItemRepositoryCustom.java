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

package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Bortnik
 */
public interface TestItemRepositoryCustom {

	Map<Long, String> selectPathNames(Long itemId);

	/**
	 * Select common items object that have provided status for
	 * specified launch
	 *
	 * @param launchId Launch
	 * @param status   Status
	 * @return List of items
	 */
	List<TestItem> selectItemsInStatusByLaunch(Long launchId, StatusEnum status);

	/**
	 * Select ids of items that has different issue from provided for
	 * specified launch
	 *
	 * @param launchId  Launch
	 * @param issueType Issue type locator
	 * @return List of item ids.
	 */
	List<Long> selectIdsNotInIssueByLaunch(Long launchId, String issueType);

	/**
	 * Select test items that has issue with provided issue type for
	 * specified launch
	 *
	 * @param launchId  Launch id
	 * @param issueType Issue type
	 * @return List of items
	 */
	List<TestItem> selectItemsInIssueByLaunch(Long launchId, String issueType);

	StatusEnum identifyStatus(Long testItemId);

	boolean hasChildren(Long testItemId);

	List<IssueType> selectIssueLocatorsByProject(Long projectId);

	/**
	 * Finishes in progress items with a interrupt status.
	 */
	void interruptInProgressItems(Long launchId);
}
