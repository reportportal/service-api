package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processBoolean;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.PluginActivityResource;

public class PluginUpdatedEvent extends AroundEvent<PluginActivityResource>
    implements ActivityEvent {

  public PluginUpdatedEvent() {
  }

  public PluginUpdatedEvent(Long userId, String userLogin, PluginActivityResource before,
      PluginActivityResource after) {
    super(userId, userLogin, before, after);
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder().addCreatedNow().addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_PLUGIN.getValue()).addPriority(EventPriority.MEDIUM)
        .addObjectId(getAfter().getId()).addObjectName(getAfter().getName())
        .addObjectType(EventObject.PLUGIN).addSubjectId(getUserId()).addSubjectName(getUserLogin())
        .addSubjectType(EventSubject.USER)
        .addHistoryField(processName(getBefore().getName(), getAfter().getName())).addHistoryField(
            processBoolean(ActivityDetailsUtil.ENABLED, getBefore().isEnabled(),
                getAfter().isEnabled()
            )).get();
  }
}
