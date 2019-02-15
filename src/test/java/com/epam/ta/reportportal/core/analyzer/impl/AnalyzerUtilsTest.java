/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.log.Log;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils.fromTestItem;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pavel Bortnik
 */
class AnalyzerUtilsTest {

	@Test
	void testConverting() {
		TestItem testItem = createTest();
		testItem.getItemResults().setIssue(createIssue(false));
		IndexTestItem indexTestItem = fromTestItem(testItem, createSameLogs(5));
		assertEquals(testItem.getItemId(), indexTestItem.getTestItemId());
		assertEquals(testItem.getUniqueId(), indexTestItem.getUniqueId());
		assertEquals(testItem.getItemResults().getIssue().getIssueType().getId(), indexTestItem.getIssueTypeId());
		assertEquals(1, indexTestItem.getLogs().size());
		assertFalse(indexTestItem.isAutoAnalyzed());
	}

	@Test
	void testConvertingAnalyzed() {
		TestItem test = createTest();
		test.getItemResults().setIssue(createIssue(true));
		IndexTestItem indexTestItem = fromTestItem(test, createSameLogs(1));
		assertTrue(indexTestItem.isAutoAnalyzed());
	}

	private TestItem createTest() {
		TestItem testItem = new TestItem();
		testItem.setItemId(1L);
		testItem.setUniqueId("uniqueId");
		testItem.setItemResults(new TestItemResults());
		return testItem;
	}

	private IssueEntity createIssue(boolean isAutoAnalyzed) {
		IssueType issueType = new IssueType();
		issueType.setId(1L);
		IssueEntity issue = new IssueEntity();
		issue.setAutoAnalyzed(isAutoAnalyzed);
		issue.setIssueType(issueType);
		return issue;
	}

	private List<Log> createSameLogs(int count) {
		List<Log> logs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Log log = new Log();
			log.setLogLevel(LogLevel.ERROR.toInt());
			log.setTestItem(new TestItem(1L));
			log.setLogMessage("Current message of the log");
			logs.add(log);
		}
		return logs;
	}

}