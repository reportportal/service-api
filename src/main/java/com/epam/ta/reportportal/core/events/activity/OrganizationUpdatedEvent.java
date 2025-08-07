/*
 * Copyright 2025 EPAM Systems
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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_ATTACHMENTS;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_LAUNCHES;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_LOGS;

import com.epam.ta.reportportal.builder.ActivityBuilder;
import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.model.activity.OrganizationAttributesActivityResource;
import java.util.Objects;
import lombok.Getter;

/**
 * Event publish when organization is updated.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Getter
public class OrganizationUpdatedEvent extends AroundEvent<OrganizationAttributesActivityResource> implements
    ActivityEvent {

  private final Long organizationId;
  private final String organizationName;

  /**
   * Constructs an OrganizationUpdatedEvent.
   *
   * @param userId          The ID of the user who performed the update.
   * @param userLogin       The login of the user who performed the update.
   * @param organizationId  The ID of the organization that was updated.
   * @param organizationName The name of the organization that was updated.
   * @param before          The state of the organization before the update.
   * @param after           The state of the organization after the update.
   */
  public OrganizationUpdatedEvent(
      Long userId,
      String userLogin,
      Long organizationId,
      String organizationName,
      OrganizationAttributesActivityResource before,
      OrganizationAttributesActivityResource after
  ) {
    super(userId, userLogin, before, after);
    this.organizationId = organizationId;
    this.organizationName = organizationName;
  }

  @Override
  public Activity toActivity() {
    return new ActivityBuilder()
        .addCreatedNow()
        .addAction(EventAction.UPDATE)
        .addEventName(ActivityAction.UPDATE_ORGANIZATION.getValue())
        .addPriority(EventPriority.HIGH)
        .addObjectId(organizationId)
        .addObjectName(organizationName)
        .addObjectType(EventObject.ORGANIZATION)
        .addOrganizationId(organizationId)
        .addSubjectId(getUserId())
        .addSubjectName(getUserLogin())
        .addSubjectType(Objects.isNull(getUserId()) ? EventSubject.APPLICATION : EventSubject.USER)
        .addHistoryField("organizationName", getBefore().getOrganizationName(), getAfter().getOrganizationName())
        .addHistoryField("organizationSlug", getBefore().getOrganizationSlug(), getAfter().getOrganizationSlug())
        .addHistoryField(processParameter(
            getBefore().getConfig(),
            getAfter().getConfig(),
            RETENTION_LAUNCHES.getName())
        )
        .addHistoryField(processParameter(
            getBefore().getConfig(),
            getAfter().getConfig(),
            RETENTION_LOGS.getName())
        )
        .addHistoryField(processParameter(
            getBefore().getConfig(),
            getAfter().getConfig(),
            RETENTION_ATTACHMENTS.getName())
        )
        .get();
  }
}