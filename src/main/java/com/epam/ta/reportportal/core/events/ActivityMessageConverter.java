/*
 * Copyright 2026 EPAM Systems
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

package com.epam.ta.reportportal.core.events;

import com.epam.ta.reportportal.entity.activity.Activity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActivityMessageConverter {

  public static ActivityMessage toMessage(Activity activity) {
    ActivityMessage message = new ActivityMessage();
    message.setCreatedAt(activity.getCreatedAt());
    message.setAction(activity.getAction());
    message.setEventName(activity.getEventName());
    message.setPriority(activity.getPriority());
    message.setObjectId(activity.getObjectId());
    message.setObjectName(activity.getObjectName());
    message.setObjectType(activity.getObjectType());
    message.setProjectId(activity.getProjectId());
    message.setDetails(activity.getDetails());
    message.setSubjectId(activity.getSubjectId());
    message.setSubjectName(activity.getSubjectName());
    message.setSubjectType(activity.getSubjectType());
    message.setSavedEvent(activity.isSavedEvent());
    return message;
  }

  public static Activity toActivity(ActivityMessage message) {
    Activity activity = new Activity();
    activity.setCreatedAt(message.getCreatedAt());
    activity.setAction(message.getAction());
    activity.setEventName(message.getEventName());
    activity.setPriority(message.getPriority());
    activity.setObjectId(message.getObjectId());
    activity.setObjectName(message.getObjectName());
    activity.setObjectType(message.getObjectType());
    activity.setProjectId(message.getProjectId());
    activity.setDetails(message.getDetails());
    activity.setSubjectId(message.getSubjectId());
    activity.setSubjectName(message.getSubjectName());
    activity.setSubjectType(message.getSubjectType());
    activity.setSavedEvent(message.isSavedEvent());
    return activity;
  }

}
