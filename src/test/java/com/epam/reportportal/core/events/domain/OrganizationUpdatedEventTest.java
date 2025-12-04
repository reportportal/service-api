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

import static com.epam.reportportal.core.events.domain.ActivityTestHelper.checkActivity;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.infrastructure.persistence.entity.activity.ActivityDetails;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventAction;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventObject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventPriority;
import com.epam.reportportal.infrastructure.persistence.entity.activity.EventSubject;
import com.epam.reportportal.infrastructure.persistence.entity.activity.HistoryField;
import com.epam.reportportal.model.activity.OrganizationAttributesActivityResource;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OrganizationUpdatedEvent}
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
class OrganizationUpdatedEventTest {

  private static Activity getExpectedActivity() {
    Activity activity = new Activity();
    activity.setAction(EventAction.UPDATE);
    activity.setEventName("updateOrganization");
    activity.setPriority(EventPriority.HIGH);
    activity.setObjectType(EventObject.ORGANIZATION);
    activity.setSubjectId(1L);
    activity.setSubjectName("user");
    activity.setSubjectType(EventSubject.USER);
    activity.setObjectId(2L);
    activity.setOrganizationId(2L);
    activity.setCreatedAt(Instant.now());
    activity.setObjectName("Updated Organization");
    ActivityDetails details = new ActivityDetails();
    details.addHistoryField(
        new HistoryField("organizationName", "Old Organization", "Updated Organization"));
    details.addHistoryField(new HistoryField("organizationSlug", "old-org", "updated-org"));
    details.addHistoryField(new HistoryField("retention_launches", "10", "30"));
    details.addHistoryField(new HistoryField("retention_logs", "5", "15"));
    details.addHistoryField(new HistoryField("retention_attachments", "3", "7"));
    activity.setDetails(details);
    return activity;
  }

  private static OrganizationAttributesActivityResource getBefore() {
    OrganizationAttributesActivityResource resource = new OrganizationAttributesActivityResource();
    resource.setOrganizationId(2L);
    resource.setOrganizationName("Old Organization");
    resource.setOrganizationSlug("old-org");
    resource.setRetention(java.util.Map.of(
        "retention_launches", "10",
        "retention_logs", "5",
        "retention_attachments", "3"
    ));
    return resource;
  }

  private static OrganizationAttributesActivityResource getAfter() {
    OrganizationAttributesActivityResource resource = new OrganizationAttributesActivityResource();
    resource.setOrganizationId(2L);
    resource.setOrganizationName("Updated Organization");
    resource.setOrganizationSlug("updated-org");
    resource.setRetention(java.util.Map.of(
        "retention_launches", "30",
        "retention_logs", "15",
        "retention_attachments", "7"
    ));
    return resource;
  }

  @Test
  void toActivity() {
    final Activity actual = new OrganizationUpdatedEvent(1L, "user", 2L, "Updated Organization",
        getBefore(), getAfter()).toActivity();
    final Activity expected = getExpectedActivity();
    checkActivity(expected, actual);
  }

  @Test
  void toActivityWhenNoNameOrSlugChangeOnlyRetentionHistory() {
    OrganizationAttributesActivityResource before = getBefore();
    OrganizationAttributesActivityResource after = getAfter();
    after.setOrganizationName(before.getOrganizationName());
    after.setOrganizationSlug(before.getOrganizationSlug());

    Activity actual = new OrganizationUpdatedEvent(1L, "user", 2L, before.getOrganizationName(),
        before, after)
        .toActivity();

    var history = actual.getDetails().getHistory();
    assertTrue(history.stream().noneMatch(h -> h.getField().equals("organizationName")));
    assertTrue(history.stream().noneMatch(h -> h.getField().equals("organizationSlug")));
    assertTrue(history.stream().anyMatch(h -> h.getField().equals("retention_launches")));
    assertTrue(history.stream().anyMatch(h -> h.getField().equals("retention_logs")));
    assertTrue(history.stream().anyMatch(h -> h.getField().equals("retention_attachments")));
  }
}
