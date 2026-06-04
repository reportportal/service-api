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

package com.epam.reportportal.base.core.events.domain.tms;

import com.epam.reportportal.base.core.events.domain.AbstractEvent;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestCaseFieldChangedEvent extends AbstractEvent<TestCaseActivityResource> {

  private String fieldName;
  private EventAction action;
  private ActivityAction activityAction;
  private Object oldValue;
  private Object newValue;

  public TestCaseFieldChangedEvent(
      TestCaseActivityResource resource,
      String fieldName,
      EventAction action,
      ActivityAction activityAction,
      Object oldValue,
      Object newValue,
      Long userId, 
      String userLogin, 
      Long organizationId) {
    super(userId, userLogin, resource, resource);
    this.organizationId = organizationId;
    this.fieldName = fieldName;
    this.action = action;
    this.activityAction = activityAction;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public void setContext(Long userId, String userLogin, Long organizationId) {
    this.userId = userId;
    this.userLogin = userLogin;
    this.organizationId = organizationId;
  }
}
