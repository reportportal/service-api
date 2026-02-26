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

package com.epam.reportportal.base.infrastructure.persistence.entity.activity;

public enum EventObject {

  INSTANCE("instance"),
  ORGANIZATION("organization"),
  LAUNCH("launch"),
  DASHBOARD("dashboard"),
  DEFECT_TYPE("defectType"),
  EMAIL_CONFIG("emailConfig"),
  FILTER("filter"),
  IMPORT("import"),
  INTEGRATION("integration"),
  ITEM_ISSUE("itemIssue"),
  PROJECT("project"),
  SHARING("sharing"),
  USER("user"),
  WIDGET("widget"),
  PATTERN("pattern"),
  INDEX("index"),
  PLUGIN("plugin"),
  INVITATION_LINK("Invitation link"),
  LOG_TYPE("logType"),
  NOTIFICATION_RULE("Notification_rule");

  private final String value;

  EventObject(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
