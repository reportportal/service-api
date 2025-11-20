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

package com.epam.reportportal.core.events.activity;

import static com.epam.reportportal.core.events.activity.util.ActivityDetailsUtil.RP_SUBJECT_NAME;

import com.epam.reportportal.core.events.ActivityEvent;
import com.epam.reportportal.infrastructure.persistence.builder.ActivityBuilder;
import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

/**
 * Event publish when organization is created.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Getter
public class OrganizationCreatedEvent extends AbstractEvent implements ActivityEvent {

  private final Long organizationId;
  private final String organizationName;
  private final Instant createdAt;

  /**
   * Constructs a new OrganizationCreatedEvent.
   *
   * @param userId           The ID of the user who created the organization.
   * @param userLogin        The login of the user who created the organization.
   * @param organizationId   The ID of the created organization.
   * @param organizationName The name of the created organization.
   */
  public OrganizationCreatedEvent(Long userId, String userLogin, Long organizationId,
      String organizationName) {
    super(userId, userLogin);
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.createdAt = Instant.now();
  }

  /**
   * Constructs a new OrganizationCreatedEvent (without userId, userLogin - system event).
   *
   * @param organizationId   The ID of the created organization.
   * @param organizationName The name of the created organization.
   */
  public OrganizationCreatedEvent(Long organizationId, String organizationName) {
    this.organizationId = organizationId;
    this.organizationName = organizationName;
    this.createdAt = Instant.now();
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedAt(createdAt)
        .addAction(EventAction.CREATE)
        .addEventName(ActivityAction.CREATE_ORGANIZATION.getValue())
        .addPriority(EventPriority.MEDIUM)
        .addObjectId(organizationId)
        .addObjectName(organizationName)
        .addObjectType(EventObject.ORGANIZATION)
        .addOrganizationId(organizationId)
        .addSubjectId(getUserId())
        .addSubjectName(Objects.isNull(getUserLogin()) ? RP_SUBJECT_NAME : getUserLogin())
        .addSubjectType(Objects.isNull(getUserId()) ? EventSubject.APPLICATION : EventSubject.USER)
        .get();
  }

}
