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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.model.IndexLog;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Useful utils methods for basic analyzer
 *
 * @author Pavel Bortnik
 */
public class AnalyzerUtils {

	private AnalyzerUtils() {
		//static only
	}

	/**
	 * Creates {@link IndexLog} model for log for further
	 * sending that into analyzer
	 */
	private static Function<Log, IndexLog> TO_INDEX_LOG = log -> {
		IndexLog indexLog = new IndexLog();
		indexLog.setLogId(log.getId());
		if (log.getLevel() != null) {
			indexLog.setLogLevel(log.getLevel().toInt());
		}
		indexLog.setMessage(log.getLogMsg());
		return indexLog;
	};

	/**
	 * Creates {@link IndexTestItem} model for test item and it's logs
	 * for further sending that into analyzer.
	 *
	 * @param testItem Test item to be created from
	 * @param logs     Test item's logs
	 * @return {@link IndexTestItem} object
	 */
	public static IndexTestItem fromTestItem(TestItem testItem, List<Log> logs) {
		IndexTestItem indexTestItem = new IndexTestItem();
		indexTestItem.setTestItemId(testItem.getId());
		indexTestItem.setUniqueId(testItem.getUniqueId());
		if (testItem.getIssue() != null) {
			indexTestItem.setIssueType(testItem.getIssue().getIssueType());
			indexTestItem.setAutoAnalyzed(testItem.getIssue().isAutoAnalyzed());
		}
		if (!logs.isEmpty()) {
			indexTestItem.setLogs(logs.stream().map(TO_INDEX_LOG).collect(Collectors.toSet()));
		}
		return indexTestItem;
	}
}
