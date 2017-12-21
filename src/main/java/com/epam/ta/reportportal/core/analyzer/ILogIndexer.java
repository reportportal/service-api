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

package com.epam.ta.reportportal.core.analyzer;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;

/**
 * Service for indexing log content in some external storage for further search/analysis.
 *
 * @author Ivan Sharamet
 */
public interface ILogIndexer {

	/**
	 * Index single log if it's level greater than
	 * {@link com.epam.ta.reportportal.database.entity.LogLevel#ERROR}
	 *
	 * @param log - log
	 */
	void indexLog(Log log);

	/**
	 * Index logs with it's level greater than
	 * {@link com.epam.ta.reportportal.database.entity.LogLevel#ERROR}
	 * for all given test items within launch
	 *
	 * @param launchId  - ID of the launch
	 * @param testItems - list of test items, for which log indexing will be performed
	 */
	void indexLogs(String launchId, List<TestItem> testItems);

	/**
	 * Delete index of specified project
	 *
	 * @param project Project/index
	 */
	void deleteIndex(String project);

	/**
	 * Remove documents with specified ids from index
	 *
	 * @param index Index to to be cleaned
	 * @param ids   Document ids to be deleted from index
	 */
	void cleanIndex(String index, List<String> ids);

	/**
	 * Index all logs with its' level greater than
	 * {@link com.epam.ta.reportportal.database.entity.LogLevel#ERROR} in repository
	 */
	void indexAllLogs();
}
