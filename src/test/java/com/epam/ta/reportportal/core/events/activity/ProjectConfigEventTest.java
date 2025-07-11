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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.OrganizationUtil.TEST_PROJECT_KEY;
import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.model.activity.ProjectAttributesActivityResource;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class ProjectConfigEventTest {

  private static final Pair<String, String> ANALYZER_MODE = Pair.of("false", "true");
  private static final Pair<String, String> MIN_SHOULD_MATCH = Pair.of("80", "100");
  private static final Pair<String, String> NUMBER_OF_LOG_LINES = Pair.of("5", "10");
  private static final Pair<String, String> AUTO_ANALYZED_ENABLED = Pair.of("false", "true");
  private static final Pair<String, String> ALL_MESSAGES_SHOULD_MATCH = Pair.of("false", "true");

  private static final Pair<String, String> KEEP_LOGS = Pair.of("1 month", "3 month");
  private static final Pair<String, String> KEEP_SCREENSHOTS = Pair.of("2 weeks", "3 weeks");
  private static final Pair<String, String> INTERRUPT_JOB_TIME = Pair.of("1 day", "1 week");

  private static Activity getExpectedActivity(EventAction action) {
    Activity activity = new Activity();
    activity.setAction(action);
    activity.setPriority(EventPriority.LOW);
    activity.setObjectType(EventObject.PROJECT);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setProjectId(3L);
    activity.setOrganizationId(1L);
    activity.setObjectId(3L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("analyzer");
    activity.setDetails(new ActivityDetails());
    return activity;
  }

  @Test
  void analyzerConfigUpdate() {
    final Activity actual = new ProjectAnalyzerConfigEvent(getProjectAttributes(
        getAnalyzerConfig(ANALYZER_MODE.getLeft(), MIN_SHOULD_MATCH.getLeft(),
            NUMBER_OF_LOG_LINES.getLeft(), AUTO_ANALYZED_ENABLED.getLeft(),
            ALL_MESSAGES_SHOULD_MATCH.getLeft()
        )), getProjectAttributes(
        getAnalyzerConfig(ANALYZER_MODE.getRight(), MIN_SHOULD_MATCH.getRight(),
            NUMBER_OF_LOG_LINES.getRight(), AUTO_ANALYZED_ENABLED.getRight(),
            ALL_MESSAGES_SHOULD_MATCH.getRight()
        )), 1L, "user", 1L).toActivity();
    final Activity expected = getExpectedActivity(EventAction.UPDATE);
    expected.getDetails().setHistory(
        getAnalyzerConfigHistory(ANALYZER_MODE, MIN_SHOULD_MATCH, NUMBER_OF_LOG_LINES,
            AUTO_ANALYZED_ENABLED, ALL_MESSAGES_SHOULD_MATCH
        ));
    expected.setEventName("updateAnalyzer");
    checkActivity(expected, actual);
  }

  private static ProjectAttributesActivityResource getProjectAttributes(
      Map<String, String> config) {
    ProjectAttributesActivityResource resource = new ProjectAttributesActivityResource();
    resource.setProjectName(TEST_PROJECT_KEY);
    resource.setProjectId(3L);
    resource.setConfig(config);
    return resource;
  }

  private static Map<String, String> getAnalyzerConfig(String analyzerMode, String minShouldMatch,
      String numberOfLogs, String autoAnalyzerEnabled, String allMessagesShouldMatch) {
    HashMap<String, String> result = new HashMap<>();
    result.put(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(), analyzerMode);
    result.put(ProjectAttributeEnum.MIN_SHOULD_MATCH.getAttribute(), minShouldMatch);
    result.put(ProjectAttributeEnum.NUMBER_OF_LOG_LINES.getAttribute(), numberOfLogs);
    result.put(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(), autoAnalyzerEnabled);
    result.put(ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH.getAttribute(),
        allMessagesShouldMatch
    );
    return result;
  }

  private static Map<String, String> getProjectConfig(String keepLogs, String keepScreenshots,
      String interruptJobTime) {
    HashMap<String, String> result = new HashMap<>();
    result.put(ProjectAttributeEnum.KEEP_LOGS.getAttribute(), keepLogs);
    result.put(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute(), keepScreenshots);
    result.put(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute(), interruptJobTime);
    return result;
  }

  @Test
  void projectConfigUpdate() {
    final Activity actual = new ProjectUpdatedEvent(getProjectAttributes(
        getProjectConfig(KEEP_LOGS.getLeft(), KEEP_SCREENSHOTS.getLeft(),
            INTERRUPT_JOB_TIME.getLeft()
        )), getProjectAttributes(getProjectConfig(KEEP_LOGS.getRight(), KEEP_SCREENSHOTS.getRight(),
        INTERRUPT_JOB_TIME.getRight()
    )), 1L, "user", 1L).toActivity();
    final Activity expected = getExpectedActivity(EventAction.UPDATE);
    expected.setPriority(EventPriority.HIGH);
    expected.getDetails()
        .setHistory(getProjectConfigHistory(KEEP_LOGS, KEEP_SCREENSHOTS, INTERRUPT_JOB_TIME));
    expected.setEventName("updateProject");
    expected.setObjectName(TEST_PROJECT_KEY);
    checkActivity(expected, actual);
  }

  private static List<HistoryField> getAnalyzerConfigHistory(Pair<String, String> analyzerMode,
      Pair<String, String> minShouldMatch, Pair<String, String> numberOfLogsLines,
      Pair<String, String> autoAnalyzed, Pair<String, String> allMessagesShouldMatch) {
    return Lists.newArrayList(
        HistoryField.of(ProjectAttributeEnum.AUTO_ANALYZER_MODE.getAttribute(),
            analyzerMode.getLeft(), analyzerMode.getRight()
        ), HistoryField.of(ProjectAttributeEnum.MIN_SHOULD_MATCH.getAttribute(),
            minShouldMatch.getLeft(), minShouldMatch.getRight()
        ), HistoryField.of(ProjectAttributeEnum.NUMBER_OF_LOG_LINES.getAttribute(),
            numberOfLogsLines.getLeft(), numberOfLogsLines.getRight()
        ), HistoryField.of(ProjectAttributeEnum.AUTO_ANALYZER_ENABLED.getAttribute(),
            autoAnalyzed.getLeft(), autoAnalyzed.getRight()
        ), HistoryField.of(ProjectAttributeEnum.ALL_MESSAGES_SHOULD_MATCH.getAttribute(),
            allMessagesShouldMatch.getLeft(), allMessagesShouldMatch.getRight()
        ));
  }

  private static List<HistoryField> getProjectConfigHistory(Pair<String, String> keepLogs,
      Pair<String, String> keepScreenshots, Pair<String, String> interruptJobTime) {
    return Lists.newArrayList(
        HistoryField.of(ProjectAttributeEnum.KEEP_LOGS.getAttribute(), keepLogs.getLeft(),
            keepLogs.getRight()
        ), HistoryField.of(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute(),
            keepScreenshots.getLeft(), keepScreenshots.getRight()
        ), HistoryField.of(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute(),
            interruptJobTime.getLeft(), interruptJobTime.getRight()
        ));
  }

}
