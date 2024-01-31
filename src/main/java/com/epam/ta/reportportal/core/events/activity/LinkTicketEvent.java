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

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.TestItemActivityResource;
import com.google.common.base.Strings;

/**
 * @author Andrei Varabyeu
 */
public class LinkTicketEvent extends AroundEvent<TestItemActivityResource> implements
    ActivityEvent {

  private boolean isLinkedByAnalyzer;

  public LinkTicketEvent() {
  }

  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin,
      boolean isLinkedByAnalyzer) {
    super(userId, userLogin, before, after);
    this.isLinkedByAnalyzer = isLinkedByAnalyzer;
  }

  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      String userLogin,
      boolean isLinkedByAnalyzer) {
    super(null, userLogin, before, after);
    this.isLinkedByAnalyzer = isLinkedByAnalyzer;
  }

  public boolean isLinkedByAnalyzer() {
    return isLinkedByAnalyzer;
  }

  public void setLinkedByAnalyzer(boolean linkedByAnalyzer) {
    isLinkedByAnalyzer = linkedByAnalyzer;
  }

  @Override
  public Activity toActivity() {
    ActivityBuilder builder = new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.LINK)
        .addEventName(isLinkedByAnalyzer
            ? ActivityAction.LINK_ISSUE_AA.getValue()
            : ActivityAction.LINK_ISSUE.getValue())
        .addPriority(EventPriority.LOW)
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addObjectType(EventObject.ITEM_ISSUE)
        .addProjectId(getAfter().getProjectId())
        .addSubjectId(isLinkedByAnalyzer ? null : getUserId())
        .addSubjectName(isLinkedByAnalyzer ? "analyzer" : getUserLogin())
        .addSubjectType(isLinkedByAnalyzer ? EventSubject.APPLICATION : EventSubject.USER);

    if (getAfter() != null) {
      String oldValue = getBefore().getTickets();
      String newValue = getAfter().getTickets();
      //no changes with tickets
      if (Strings.isNullOrEmpty(oldValue) && newValue.isEmpty() || oldValue.equalsIgnoreCase(
          newValue)) {
        return null;
      }
      if (!oldValue.isEmpty() && !newValue.isEmpty() || !oldValue.equalsIgnoreCase(newValue)) {
        if (oldValue.length() > newValue.length()) {
          builder.addAction(EventAction.UNLINK);
          builder.addEventName(ActivityAction.UNLINK_ISSUE.getValue());
        }
        builder.addHistoryField(TICKET_ID, oldValue, newValue);
      }
    }

    return builder.get();

  }

}
