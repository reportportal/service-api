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
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.TICKET;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.LINK_ISSUE;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.LINK_ISSUE_AA;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UNLINK_ISSUE;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.google.common.base.Strings;
import java.util.Arrays;

/**
 * @author Andrei Varabyeu
 */
public class LinkTicketEvent extends AroundEvent<TestItemActivityResource> implements
    ActivityEvent {

  private ActivityAction activityAction;

  public LinkTicketEvent() {
  }

  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      Long userId, String userLogin,
      ActivityAction activityAction) {
    super(userId, userLogin, before, after);
    if (!Arrays.asList(LINK_ISSUE_AA, LINK_ISSUE).contains(activityAction)) {
      throw new IllegalArgumentException(
          "Activity action '" + activityAction + "' is not supported");
    }
    this.activityAction = activityAction;
  }

  public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after,
      String userLogin,
      ActivityAction activityAction) {
    super(null, userLogin, before, after);
    if (!Arrays.asList(LINK_ISSUE_AA, LINK_ISSUE).contains(activityAction)) {
      throw new IllegalArgumentException(
          "Activity action '" + activityAction + "' is not supported");
    }
    this.activityAction = activityAction;
  }

  public ActivityAction getActivityAction() {
    return activityAction;
  }

  public void setActivityAction(ActivityAction activityAction) {
    this.activityAction = activityAction;
  }

  @Override
  public Activity toActivity() {
    ActivityBuilder builder = new ActivityBuilder().addCreatedNow()
        .addAction(activityAction)
        .addActivityEntityType(TICKET)
        .addUserId(getUserId())
        .addUserName(getUserLogin())
        .addObjectId(getAfter().getId())
        .addObjectName(getAfter().getName())
        .addProjectId(getAfter().getProjectId());

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
          builder.addAction(UNLINK_ISSUE);
        }
        builder.addHistoryField(TICKET_ID, oldValue, newValue);
      }
    }

    return builder.get();

  }
}
