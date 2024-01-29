package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.PluginActivityResource;

public class PluginUploadedEvent extends AbstractEvent implements ActivityEvent {

  private PluginActivityResource pluginActivityResource;

  public PluginUploadedEvent() {
  }

  public PluginUploadedEvent(PluginActivityResource pluginActivityResource, Long userId,
      String userLogin) {
    super(userId, userLogin);
    this.pluginActivityResource = pluginActivityResource;
  }

  public PluginActivityResource getPluginActivityResource() {
    return pluginActivityResource;
  }

  public void setPluginActivityResource(PluginActivityResource pluginActivityResource) {
    this.pluginActivityResource = pluginActivityResource;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow().addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_PLUGIN.getValue())
        .addObjectId(pluginActivityResource.getId()).addObjectName(pluginActivityResource.getName())
        .addObjectType(EventObject.PLUGIN).addSubjectId(getUserId()).addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER).addPriority(EventPriority.CRITICAL).get();
  }
}
