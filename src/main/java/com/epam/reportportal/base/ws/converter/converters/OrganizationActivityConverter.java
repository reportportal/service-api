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

package com.epam.reportportal.base.ws.converter.converters;

import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicy;
import com.epam.reportportal.base.core.organization.settings.OrganizationSettingsEnum;
import com.epam.reportportal.base.model.activity.OrganizationAttributesActivityResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for converting organization information and retention policy settings into
 * {@link com.epam.reportportal.base.model.activity.OrganizationAttributesActivityResource} used in activity events.
 */
public final class OrganizationActivityConverter {

  private OrganizationActivityConverter() {
    // static only
  }

  /**
   * Builds an {@link OrganizationAttributesActivityResource} from the provided organization identifiers and retention
   * policy.
   *
   * @param orgId  the organization id
   * @param name   the organization name
   * @param slug   the organization slug
   * @param policy the retention policy to apply
   * @return populated activity resource describing the organization attributes
   */
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


