package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.ws.model.activity.UserActivityResource;

public class UserDeletedEvent extends BeforeEvent<UserActivityResource> implements ActivityEvent {

  public UserDeletedEvent() {
  }

  public UserDeletedEvent(UserActivityResource userActivityResource, Long userId, String username) {
    super(userId, username, userActivityResource);
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow().addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_USER.getValue()).addObjectId(getBefore().getId())
        .addObjectName(getBefore().getFullName()).addObjectType(EventObject.USER)
        .addSubjectId(getUserId()).addSubjectName(getUserLogin()).addSubjectType(EventSubject.USER)
        .addPriority(EventPriority.HIGH).get();
  }
}
