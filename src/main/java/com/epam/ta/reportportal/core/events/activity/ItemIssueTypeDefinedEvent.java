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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.COMMENT;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.IGNORE_ANALYZER;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.ISSUE_TYPE;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.RELEVANT_ITEM;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.model.analyzer.RelevantItemInfo;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Andrei Varabyeu
 */
@Setter
@Getter
public class ItemIssueTypeDefinedEvent extends AroundEvent<TestItemActivityResource> implements
    ActivityEvent {

  private RelevantItemInfo relevantItemInfo;
  private Long orgId;

  public ItemIssueTypeDefinedEvent() {
  }

  public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin, Long  orgId) {
    super(userId, userLogin, before, after);
    this.orgId = orgId;
  }

  public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after,
      String userLogin,
      RelevantItemInfo relevantItemInfo, Long orgId) {
    super(null, userLogin, before, after);
    this.relevantItemInfo = relevantItemInfo;
    this.orgId = orgId;
  }

  public boolean isAutoAnalyzed() {
    return getAfter().isAutoAnalyzed();
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.ANALYZE)
        .addEventName(getAfter().isAutoAnalyzed()
            ? ActivityAction.ANALYZE_ITEM.getValue()
            : ActivityAction.UPDATE_ITEM.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(getAfter().getProjectId())
        .addOrganizationId(orgId)
        .addSubjectId(isAutoAnalyzed() ? null : getUserId())
        .addSubjectName(isAutoAnalyzed() ? "analyzer" : getUserLogin())
        .addSubjectType(isAutoAnalyzed() ? EventSubject.APPLICATION : EventSubject.USER)
        .addHistoryField(processIssueDescription(getBefore().getIssueDescription(),
            getAfter().getIssueDescription()))
        .addHistoryField(processIssueTypes(getBefore().getIssueTypeLongName(),
            getAfter().getIssueTypeLongName()))
        .addHistoryField(
            processIgnoredAnalyzer(getBefore().isIgnoreAnalyzer(), getAfter().isIgnoreAnalyzer()))
        .addHistoryField(processRelevantItem(relevantItemInfo))
        .get();
  }

  private Optional<HistoryField> processIssueDescription(String before, String after) {
    HistoryField historyField = null;

    after = (null != after) ? after.trim() : EMPTY_STRING;
    before = (null != before) ? before : EMPTY_STRING;

    if (!before.equals(after)) {
      historyField = HistoryField.of(COMMENT, before, after);
    }
    return Optional.ofNullable(historyField);
  }

  private Optional<HistoryField> processIssueTypes(String before, String after) {
    return before.equalsIgnoreCase(after) ? Optional.empty()
        : Optional.of(HistoryField.of(ISSUE_TYPE, before, after));
  }

  private Optional<HistoryField> processIgnoredAnalyzer(Boolean before, Boolean after) {
    if (!before.equals(after)) {
      return Optional.of(
          HistoryField.of(IGNORE_ANALYZER, String.valueOf(before), String.valueOf(after)));
    }
    return Optional.empty();
  }

  private Optional<HistoryField> processRelevantItem(RelevantItemInfo relevantItemInfo) {
    if (null == relevantItemInfo) {
      return Optional.empty();
    }
    return Optional.of(HistoryField.of(RELEVANT_ITEM, null, relevantItemInfo.toString()));
  }

}
