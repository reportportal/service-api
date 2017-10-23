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

package com.epam.ta.reportportal.util.analyzer.model;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.epam.ta.reportportal.util.analyzer.AnalyzerUtils.fromTestItem;

/**
 * @author Pavel Bortnik
 */
public class AnalyzerUtils {

	@Test
	public void testConverting() {
		TestItem testItem = new TestItem();
		testItem.setId("item");
		testItem.setUniqueId("uniqueId");
		testItem.setIssue(new TestItemIssue());
		IndexTestItem indexTestItem = fromTestItem(testItem, createSameLogs(5));
		Assert.assertEquals(testItem.getId(), indexTestItem.getTestItemId());
		Assert.assertEquals(testItem.getUniqueId(), indexTestItem.getUniqueId());
		Assert.assertEquals(testItem.getIssue().getIssueType(), indexTestItem.getIssueType());
		Assert.assertEquals(indexTestItem.getLogs().size(), 1);
	}

	private List<Log> createSameLogs(int count) {
		List<Log> logs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Log log = new Log();
			log.setLevel(LogLevel.ERROR);
			log.setTestItemRef("item");
			log.setLogMsg("Current message of the log");
			logs.add(log);
		}
		return logs;
	}


}