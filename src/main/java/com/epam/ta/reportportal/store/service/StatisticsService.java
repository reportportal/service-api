/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.service;

import com.epam.ta.reportportal.store.database.dao.TestItemResultsRepository;
import com.epam.ta.reportportal.store.database.entity.enums.StatusEnum;
import com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.launch.StatisticEntry;
import com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter;
import com.epam.ta.reportportal.ws.model.statistics.IssueCounter;
import com.epam.ta.reportportal.ws.model.statistics.Statistics;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Dzianis_Shybeka
 */
@Service
public class StatisticsService {

	private static final String GROUP_TOTAL = "total";

	private final TestItemResultsRepository testItemResultsRepository;

	public StatisticsService(TestItemResultsRepository testItemResultsRepository) {
		this.testItemResultsRepository = testItemResultsRepository;
	}

	// TODO: 6/4/2018 does group total always equal to count for type?
	public Statistics getStatistics(TestItem entity) {

		Statistics statistics = new Statistics();

		statistics.setExecutions(getExecutionCounter(entity));

		statistics.setDefects(getIssueCounter(entity));

		return statistics;
	}

	private IssueCounter getIssueCounter(TestItem entity) {

		Map<String, Long> issueTypeToCount = testItemResultsRepository.issueCounter(entity.getItemId())
				.stream()
				.collect(Collectors.toMap(StatisticEntry::getName, StatisticEntry::getCount));

		IssueCounter issueCounter = new IssueCounter();

		int productBugCount = issueTypeToCount.get(TestItemIssueType.PRODUCT_BUG.toString()).intValue();
		issueCounter.setProductBug(ImmutableMap.of(GROUP_TOTAL,
				productBugCount,
				TestItemIssueType.PRODUCT_BUG.getLocator(),
				productBugCount
		));

		int automationBugCount = issueTypeToCount.get(TestItemIssueType.AUTOMATION_BUG.toString()).intValue();
		issueCounter.setAutomationBug(ImmutableMap.of(GROUP_TOTAL,
				automationBugCount,
				TestItemIssueType.AUTOMATION_BUG.getLocator(),
				automationBugCount
		));

		int noDefectCount = issueTypeToCount.get(TestItemIssueType.NO_DEFECT.toString()).intValue();
		issueCounter.setNoDefect(ImmutableMap.of(GROUP_TOTAL, noDefectCount, TestItemIssueType.NO_DEFECT.getLocator(), noDefectCount));

		int systemIssueCount = issueTypeToCount.get(TestItemIssueType.SYSTEM_ISSUE.toString()).intValue();
		issueCounter.setSystemIssue(ImmutableMap.of(GROUP_TOTAL,
				systemIssueCount,
				TestItemIssueType.SYSTEM_ISSUE.getLocator(),
				systemIssueCount
		));

		int toInvestigateCount = issueTypeToCount.get(TestItemIssueType.TO_INVESTIGATE.toString()).intValue();
		issueCounter.setToInvestigate(ImmutableMap.of(GROUP_TOTAL,
				toInvestigateCount,
				TestItemIssueType.TO_INVESTIGATE.getLocator(),
				toInvestigateCount
		));

		return issueCounter;
	}

	private ExecutionCounter getExecutionCounter(TestItem entity) {

		ExecutionCounter executions = new ExecutionCounter();
		Map<String, Long> executionTypeToCount = testItemResultsRepository.executionCounter(entity.getItemId())
				.stream()
				.collect(Collectors.toMap(StatisticEntry::getName, StatisticEntry::getCount));

		executions.setFailed(Objects.toString(executionTypeToCount.get(StatusEnum.FAILED.toString()), StringUtils.EMPTY));
		executions.setPassed(Objects.toString(executionTypeToCount.get(StatusEnum.PASSED.toString()), StringUtils.EMPTY));
		executions.setSkipped(Objects.toString(executionTypeToCount.get(StatusEnum.SKIPPED.toString()), StringUtils.EMPTY));
		executions.setTotal(Objects.toString(
				executionTypeToCount.entrySet().stream().mapToLong(Map.Entry::getValue).sum(),
				StringUtils.EMPTY
		));

		return executions;
	}
}
