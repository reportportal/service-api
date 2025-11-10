/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.reportportal.infrastructure.persistence.builder;

import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;


public class ActivityBuilder implements Supplier<Activity> {

  public static final int MAX_OBJECT_NAME_LENGTH = 128;
  private final Activity activity;
  private ActivityDetails details;

  public ActivityBuilder() {
    activity = new Activity();
    details = new ActivityDetails();
  }

  public ActivityBuilder addSubjectId(Long subjectId) {
    activity.setSubjectId(subjectId);
    return this;
  }

  public ActivityBuilder addSubjectName(String postedName) {
    activity.setSubjectName(postedName);
    return this;
  }

  public ActivityBuilder addProjectId(Long projectId) {
    activity.setProjectId(projectId);
    return this;
  }

  public ActivityBuilder addObjectType(EventObject objectType) {
    activity.setObjectType(objectType);
    return this;
  }

  public ActivityBuilder addAction(EventAction action) {
    activity.setAction(action);
    return this;
  }

  public ActivityBuilder addDetails(ActivityDetails details) {
    this.details = details;
    return this;
  }

  public ActivityBuilder addObjectName(String name) {
    if (name != null && name.length() > MAX_OBJECT_NAME_LENGTH) {
      name = name.substring(0, MAX_OBJECT_NAME_LENGTH - 3) + "...";
    }
    activity.setObjectName(name);
    return this;
  }

  public ActivityBuilder addHistoryField(String field, String before, String after) {
    details.addHistoryField(HistoryField.of(field, before, after));
    return this;
  }

  public ActivityBuilder addHistoryField(Optional<HistoryField> historyField) {
    historyField.ifPresent(it -> details.addHistoryField(it));
    return this;
  }

  public ActivityBuilder addCreatedAt(Instant instant) {
    activity.setCreatedAt(instant);
    return this;
  }

  public ActivityBuilder addCreatedNow() {
    activity.setCreatedAt(Instant.now());
    return this;
  }

  public ActivityBuilder addObjectId(Long objectId) {
    activity.setObjectId(objectId);
    return this;
  }

  public ActivityBuilder addOrganizationId(Long orgId) {
    activity.setOrganizationId(orgId);
    return this;
  }

  public ActivityBuilder addPriority(EventPriority priority) {
    activity.setPriority(priority);
    return this;
  }

  public ActivityBuilder addSubjectType(EventSubject eventSubject) {
    activity.setSubjectType(eventSubject);
    return this;
  }

  public ActivityBuilder addEventName(String eventName) {
    activity.setEventName(eventName);
    return this;
  }

  public ActivityBuilder notSavedEvent() {
    activity.setSavedEvent(false);
    return this;
  }

  @Override
  public Activity get() {
    activity.setDetails(details);
    return activity;
  }
}
