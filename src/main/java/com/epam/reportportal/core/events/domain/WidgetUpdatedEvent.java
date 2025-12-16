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

package com.epam.reportportal.core.events.domain;

import com.epam.reportportal.model.activity.WidgetActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published when a widget is updated.
 *
 * @author Andrei Varabyeu
 */
@Getter
@NoArgsConstructor
public class WidgetUpdatedEvent extends AbstractEvent<WidgetActivityResource> {

  private String widgetOptionsBefore;
  private String widgetOptionsAfter;

  /**
   * Constructs a WidgetUpdatedEvent.
   *
   * @param before The widget state before the update
   * @param after The widget state after the update
   * @param widgetOptionsBefore The widget options before the update
   * @param widgetOptionsAfter The widget options after the update
   * @param userId The ID of the user who updated the widget
   * @param userLogin The login of the user who updated the widget
   * @param orgId The organization ID
   */
  public WidgetUpdatedEvent(WidgetActivityResource before, WidgetActivityResource after,
      String widgetOptionsBefore, String widgetOptionsAfter, Long userId, String userLogin,
      Long orgId) {
    super(userId, userLogin, before, after);
    this.widgetOptionsBefore = widgetOptionsBefore;
    this.widgetOptionsAfter = widgetOptionsAfter;
    this.organizationId = orgId;
  }

}
