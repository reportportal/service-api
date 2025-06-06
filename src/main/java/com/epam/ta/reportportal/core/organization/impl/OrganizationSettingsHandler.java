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

package com.epam.ta.reportportal.core.organization.impl;

import com.epam.reportportal.api.model.OrganizationSettings;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicy;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyAttachments;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyLaunches;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyLogs;
import org.springframework.stereotype.Service;

/**
 * Mock implementation
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>\
 **/
@Service
public class OrganizationSettingsHandler {

  public OrganizationSettings getOrganizationSettings(Long orgId) {
    return OrganizationSettings.builder().retentionPolicy(OrganizationSettingsRetentionPolicy.builder().launches(
        OrganizationSettingsRetentionPolicyLaunches.builder().build()).logs(
        OrganizationSettingsRetentionPolicyLogs.builder().build()).attachments(
        OrganizationSettingsRetentionPolicyAttachments.builder().build()).build()).build();
  }

}
