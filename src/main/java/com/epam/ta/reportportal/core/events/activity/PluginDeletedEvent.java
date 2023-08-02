package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.ws.model.activity.PluginActivityResource;

public class PluginDeletedEvent extends BeforeEvent<PluginActivityResource>
    implements ActivityEvent {
  public PluginDeletedEvent() {
  }

  public PluginDeletedEvent(PluginActivityResource pluginActivityResource, Long userId,
      String userLogin) {
    super(userId, userLogin, pluginActivityResource);
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow().addAction(EventAction.DELETE)
        .addEventName(ActivityAction.DELETE_PLUGIN.getValue())
        .addObjectId(getBefore().getId()).addObjectName(getBefore().getName())
        .addObjectType(EventObject.PLUGIN).addSubjectId(getUserId()).addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER).addPriority(EventPriority.CRITICAL).get();
  }
}
