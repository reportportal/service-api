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

package com.epam.ta.reportportal.core.statistics;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.TestItem;

/**
 * Facade for test item statistics processing
 *
 * @author Dzianis Shlychkou
 * @author Andrei_Ramanchuk
 */
public interface StatisticsFacade {

	/**
	 * Increments execution statistics (total, passed, failed and skipped) of
	 * test item and all it's ancestors
	 *
	 * @param testItem
	 * @return updated test item
	 */
	TestItem updateExecutionStatistics(TestItem testItem);

	/**
	 * Increments issue types statistics (product, automation bugs, system
	 * issues and to investigate) of test item and all it's ancestors
	 *
	 * @param testItem
	 * @return updated test item
	 */
	TestItem updateIssueStatistics(TestItem testItem);

	/**
	 * Decrements issue types statistics (product, automation bugs, system
	 * issues and to investigate) of test item and all it's ancestors, based on
	 * it's current issue type
	 *
	 * @param testItem
	 * @return updated test item
	 */
	TestItem resetIssueStatistics(TestItem testItem);

	/**
	 * Decrements execution statistics (total, passed, failed and skipped) of
	 * test item and all it's ancestors
	 *
	 * @param testItem
	 * @return updated test item
	 */
	TestItem resetExecutionStatistics(TestItem testItem);

	/**
	 * Remove issue statistics (product, automation, system bugs and
	 * to_investigate) of removed test item and all it's ancestors
	 *
	 * @param testItem
	 * @return
	 */
	TestItem deleteIssueStatistics(TestItem testItem);

	/**
	 * Remove execution statistics (total, passed, failed and skipped) of test
	 * item and all it's ancestors
	 *
	 * @param testItem
	 * @return
	 */
	TestItem deleteExecutionStatistics(TestItem testItem);

	/**
	 * Recursively updates launch and parent test items status based on
	 * statistics
	 *
	 * @param item
	 */
	void updateParentStatusFromStatistics(TestItem item);

	/**
	 * Updates launch statistics
	 *
	 * @param launch
	 */
	void updateLaunchFromStatistics(Launch launch);

	/**
	 * Recalculate launch statistics
	 *
	 * @param launch
	 */
	void recalculateStatistics(Launch launch);

	/**
	 * Set status for testItem based on strategy and returns
	 * object with exposed value.
	 *
	 * @param testItem to be identified
	 * @return TestItem object with provided status
	 */
	TestItem identifyStatus(TestItem testItem);

	/**
	 * Checks if the test item can have issue. Based on
	 * statistics calculating strategy
	 *
	 * @param testItem
	 * @return
	 */
	boolean awareIssue(TestItem testItem);
}