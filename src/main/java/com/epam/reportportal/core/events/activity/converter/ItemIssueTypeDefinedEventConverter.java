/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.events.activity.converter;

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.COMMENT;
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.IGNORE_ANALYZER;
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.ISSUE_TYPE;
import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.RELEVANT_ITEM;

import com.epam.reportportal.core.events.domain.ItemIssueTypeDefinedEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Converter for ItemIssueTypeDefinedEvent to Activity.
 */
@Component
public class ItemIssueTypeDefinedEventConverter implements
    EventToActivityConverter<ItemIssueTypeDefinedEvent> {

  @Override
  public Activity convert(ItemIssueTypeDefinedEvent event) {
    boolean isAutoAnalyzed = event.isAutoAnalyzed();
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.ANALYZE)
        .addEventName(isAutoAnalyzed
            ? ActivityAction.ANALYZE_ITEM.getValue()
            : ActivityAction.UPDATE_ITEM.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(event.getAfter().getId())
        .addObjectName(event.getAfter().getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(event.getAfter().getProjectId())
        .addOrganizationId(event.getOrganizationId())
        .addSubjectId(isAutoAnalyzed ? null : event.getUserId())
        .addSubjectName(isAutoAnalyzed ? "analyzer" : event.getUserLogin())
        .addSubjectType(isAutoAnalyzed ? EventSubject.APPLICATION : EventSubject.USER)
        .addHistoryField(processIssueDescription(event.getBefore().getIssueDescription(),
            event.getAfter().getIssueDescription()))
        .addHistoryField(processIssueTypes(event.getBefore().getIssueTypeLongName(),
            event.getAfter().getIssueTypeLongName()))
        .addHistoryField(
            processIgnoredAnalyzer(event.getBefore().isIgnoreAnalyzer(),
                event.getAfter().isIgnoreAnalyzer()))
        .addHistoryField(processRelevantItem(event.getRelevantItemInfo()))
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

  private Optional<HistoryField> processRelevantItem(
      com.epam.reportportal.model.analyzer.RelevantItemInfo relevantItemInfo) {
    if (null == relevantItemInfo) {
      return Optional.empty();
    }
    return Optional.of(HistoryField.of(RELEVANT_ITEM, null, relevantItemInfo.toString()));
  }

  @Override
  public Class<ItemIssueTypeDefinedEvent> getEventClass() {
    return ItemIssueTypeDefinedEvent.class;
  }
}
