/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicy;
import com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum;
import com.epam.ta.reportportal.model.activity.OrganizationAttributesActivityResource;
import java.util.HashMap;
import java.util.Map;

public final class OrganizationActivityConverter {

  private OrganizationActivityConverter() {
    // static only
  }

  public static OrganizationAttributesActivityResource toAttributes(Long orgId, String name, String slug,
      OrganizationSettingsRetentionPolicy policy) {
    OrganizationAttributesActivityResource resource = new OrganizationAttributesActivityResource();
    resource.setOrganizationId(orgId);
    resource.setOrganizationName(name);
    resource.setOrganizationSlug(slug);
    resource.setRetention(buildRetentionPoliciesMap(policy));
    return resource;
  }

  private static Map<String, String> buildRetentionPoliciesMap(OrganizationSettingsRetentionPolicy policy) {
    Map<String, String> retention = new HashMap<>();
    retention.put(OrganizationSettingsEnum.RETENTION_LAUNCHES.getName(),
        String.valueOf(policy.getLaunches().getPeriod()));
    retention.put(OrganizationSettingsEnum.RETENTION_LOGS.getName(),
        String.valueOf(policy.getLogs().getPeriod()));
    retention.put(OrganizationSettingsEnum.RETENTION_ATTACHMENTS.getName(),
        String.valueOf(policy.getAttachments().getPeriod()));
    return retention;
  }

}


