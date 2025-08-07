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

package com.epam.ta.reportportal.core.events.activity;

import static com.epam.ta.reportportal.core.events.activity.ActivityTestHelper.checkActivity;

import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.entity.activity.ActivityDetails;
import com.epam.ta.reportportal.entity.activity.EventAction;
import com.epam.ta.reportportal.entity.activity.EventObject;
import com.epam.ta.reportportal.entity.activity.EventPriority;
import com.epam.ta.reportportal.entity.activity.EventSubject;
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.model.activity.OrganizationAttributesActivityResource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
    details.addHistoryField(new HistoryField("organizationName", "Old Organization", "Updated Organization"));
    details.addHistoryField(new HistoryField("organizationSlug", "old-org", "updated-org"));
    details.addHistoryField(new HistoryField("retention_launches", "30", "60"));
    details.addHistoryField(new HistoryField("retention_logs", "7", "14"));
    details.addHistoryField(new HistoryField("retention_attachments", "30", "90"));
    activity.setDetails(details);
    return activity;
  }

  private static OrganizationAttributesActivityResource getBefore() {
    OrganizationAttributesActivityResource resource = new OrganizationAttributesActivityResource();
    resource.setOrganizationId(2L);
    resource.setOrganizationName("Old Organization");
    resource.setOrganizationSlug("old-org");
    Map<String, String> config = new HashMap<>();
    config.put("retention_launches", "30");
    config.put("retention_logs", "7");
    config.put("retention_attachments", "30");
    resource.setConfig(config);
    return resource;
  }

  private static OrganizationAttributesActivityResource getAfter() {
    OrganizationAttributesActivityResource resource = new OrganizationAttributesActivityResource();
    resource.setOrganizationId(2L);
    resource.setOrganizationName("Updated Organization");
    resource.setOrganizationSlug("updated-org");
    Map<String, String> config = new HashMap<>();
    config.put("retention_launches", "60");
    config.put("retention_logs", "14");
    config.put("retention_attachments", "90");
    resource.setConfig(config);
    return resource;
  }

  @Test
  void toActivity() {
    final Activity actual = new OrganizationUpdatedEvent(1L, "user", 2L, "Updated Organization", getBefore(), getAfter()).toActivity();
    final Activity expected = getExpectedActivity();
    checkActivity(expected, actual);
  }
}