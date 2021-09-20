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

import com.epam.ta.reportportal.entity.attribute.Attribute;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.fromTestItem;
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
        assertEquals(testItem.getStartTime(), indexTestItem.getStartTime());
        assertEquals(testItem.getItemResults().getIssue().getIssueType().getLocator(), indexTestItem.getIssueTypeLocator());
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

    @Test
    void testAnalyzerConfig() {
        AnalyzerConfig config = AnalyzerUtils.getAnalyzerConfig(project());
        assertEquals(String.valueOf(config.getIsAutoAnalyzerEnabled()), ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getDefaultValue());
        assertEquals(String.valueOf(config.getNumberOfLogLines()), ProjectAttributeEnum.NUMBER_OF_LOG_LINES.getDefaultValue());
        assertEquals(config.getAnalyzerMode(), ProjectAttributeEnum.AUTO_ANALYZER_MODE.getDefaultValue());
        assertEquals(String.valueOf(config.getMinShouldMatch()), ProjectAttributeEnum.MIN_SHOULD_MATCH.getDefaultValue());
        assertEquals(String.valueOf(config.isIndexingRunning()), ProjectAttributeEnum.INDEXING_RUNNING.getDefaultValue());
        assertEquals(String.valueOf(config.isAllMessagesShouldMatch()), ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH.getDefaultValue());
    }

    private TestItem createTest() {
        TestItem testItem = new TestItem();
        testItem.setItemId(1L);
        testItem.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
        testItem.setUniqueId("uniqueId");
        testItem.setItemResults(new TestItemResults());
        return testItem;
    }

    private IssueEntity createIssue(boolean isAutoAnalyzed) {
        IssueType issueType = new IssueType();
        issueType.setId(1L);
        issueType.setLocator("locator");
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

    public static Project project() {
        Project project = new Project();
        project.setProjectAttributes(ProjectUtils.defaultProjectAttributes(project, Arrays.stream(ProjectAttributeEnum.values()).map(it -> {
            Attribute attribute = new Attribute();
            attribute.setName(it.getAttribute());
            return attribute;
        }).collect(Collectors.toSet())));
        return project;
    }

}