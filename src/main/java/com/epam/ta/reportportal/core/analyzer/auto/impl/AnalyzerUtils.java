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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.analyzer.RelevantItemInfo;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.epam.ta.reportportal.ws.model.project.UniqueErrorConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;
import static java.util.Optional.ofNullable;

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
	private final static Function<LogFull, IndexLog> TO_INDEX_LOG = log -> {
		IndexLog indexLog = new IndexLog();
		indexLog.setLogId(log.getId());
		if (log.getLogLevel() != null) {
			indexLog.setLogLevel(log.getLogLevel());
		}
		indexLog.setMessage(log.getLogMessage());
		indexLog.setClusterId(log.getClusterId());
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
	public static IndexTestItem fromTestItem(TestItem testItem) {
		IndexTestItem indexTestItem = new IndexTestItem();
		indexTestItem.setTestItemId(testItem.getItemId());
		indexTestItem.setTestItemName(testItem.getName());
		indexTestItem.setUniqueId(testItem.getUniqueId());
		indexTestItem.setStartTime(testItem.getStartTime());
		indexTestItem.setTestCaseHash(testItem.getTestCaseHash());
		if (testItem.getItemResults().getIssue() != null) {
			indexTestItem.setIssueTypeLocator(testItem.getItemResults().getIssue().getIssueType().getLocator());
			indexTestItem.setAutoAnalyzed(testItem.getItemResults().getIssue().getAutoAnalyzed());
		}
		return indexTestItem;
	}

	public static Set<IndexLog> fromLogs(List<LogFull> logs) {
		return logs.stream().filter(it -> StringUtils.isNotEmpty(it.getLogMessage())).map(TO_INDEX_LOG).collect(Collectors.toSet());
	}

	public static AnalyzerConfig getAnalyzerConfig(Project project) {
		Map<String, String> configParameters = ProjectUtils.getConfigParameters(project.getProjectAttributes());
		return getAnalyzerConfig(configParameters);
	}

	public static AnalyzerConfig getAnalyzerConfig(Map<String, String> configParameters) {
		AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setIsAutoAnalyzerEnabled(BooleanUtils.toBoolean(configParameters.get(AUTO_ANALYZER_ENABLED.getAttribute())));
		analyzerConfig.setMinShouldMatch(Integer.valueOf(ofNullable(configParameters.get(MIN_SHOULD_MATCH.getAttribute())).orElse(
				MIN_SHOULD_MATCH.getDefaultValue())));
		analyzerConfig.setSearchLogsMinShouldMatch(Integer.valueOf(ofNullable(configParameters.get(SEARCH_LOGS_MIN_SHOULD_MATCH.getAttribute())).orElse(
				SEARCH_LOGS_MIN_SHOULD_MATCH.getDefaultValue())));
		analyzerConfig.setNumberOfLogLines(Integer.valueOf(ofNullable(configParameters.get(NUMBER_OF_LOG_LINES.getAttribute())).orElse(
				NUMBER_OF_LOG_LINES.getDefaultValue())));
		analyzerConfig.setIndexingRunning(BooleanUtils.toBoolean(configParameters.get(INDEXING_RUNNING.getAttribute())));
		analyzerConfig.setAnalyzerMode(configParameters.get(AUTO_ANALYZER_MODE.getAttribute()));
		analyzerConfig.setAllMessagesShouldMatch(BooleanUtils.toBoolean(configParameters.get(ALL_MESSAGES_SHOULD_MATCH.getAttribute())));
		return analyzerConfig;
	}

	public static UniqueErrorConfig getUniqueErrorConfig(Map<String, String> configParameters) {
		final UniqueErrorConfig uniqueErrorConfig = new UniqueErrorConfig();
		uniqueErrorConfig.setEnabled(BooleanUtils.toBoolean(configParameters.get(AUTO_UNIQUE_ERROR_ANALYZER_ENABLED.getAttribute())));
		uniqueErrorConfig.setRemoveNumbers(BooleanUtils.toBoolean(configParameters.get(UNIQUE_ERROR_ANALYZER_REMOVE_NUMBERS.getAttribute())));
		return uniqueErrorConfig;
	}

	public static final Function<TestItem, RelevantItemInfo> TO_RELEVANT_ITEM_INFO = item -> {
		RelevantItemInfo relevantItemInfo = new RelevantItemInfo();
		relevantItemInfo.setItemId(String.valueOf(item.getItemId()));
		relevantItemInfo.setPath(item.getPath());
		relevantItemInfo.setLaunchId(String.valueOf(item.getLaunchId()));
		return relevantItemInfo;
	};
}
