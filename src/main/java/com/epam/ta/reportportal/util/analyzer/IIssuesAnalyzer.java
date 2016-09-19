/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

package com.epam.ta.reportportal.util.analyzer;

import java.util.List;

import com.epam.ta.reportportal.database.entity.item.TestItem;

/**
 * Interface declaration of analyzer methods
 * 
 * @author Andrei_Ramanchuk
 */
public interface IIssuesAnalyzer {

	/**
	 * Collect all failed and investigated test items from launch history
	 * 
	 * @param depth
	 *            - depth of history searching
	 * @param launchId
	 *            - current processing launch ID
	 * @param projectName
	 *            - working project name
	 * @return List<TestItem>
	 */
	List<TestItem> collectPreviousIssues(int depth, String launchId, String projectName);

	/**
	 * Analyze history to find similar issues
	 * 
	 * @param launchId
	 *            ID of initial launch for history
	 * @param resources
	 *            - current test items with failed\skipped status and issue
	 * @param scope
	 *            - bunch of history items with investigated results
	 */
	void analyze(String launchId, List<TestItem> resources, List<TestItem> scope);

	/**
	 * Validate possibility to analyze launch with KEY id
	 * 
	 * @param key
	 *            launch ID
	 * @return <b>true</b> if launch possible to be analyzed, <b>false</b> - if
	 *         not
	 */
	boolean isPossible(String key);

	/**
	 * Remove marker from cache that launch analyze finished
	 * 
	 * @param key
	 *            Launch id as key in cache
	 */
	void analyzeFinished(String key);

	/**
	 * Put launch ID in processing analyzer cache
	 * 
	 * @param key
	 *            Launch ID which should be places in AA launches cache
	 * @return <b>true</b> if launch under analyze process and <b>false</b>
	 *         otherwise
	 */
	boolean analyzeStarted(String key);
}