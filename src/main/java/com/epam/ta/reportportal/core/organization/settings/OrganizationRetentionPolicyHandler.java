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

package com.epam.ta.reportportal.core.organization.settings;

import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_ATTACHMENTS;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_LAUNCHES;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_LOGS;

import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicy;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyAttachments;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyLaunches;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyLogs;
import com.epam.ta.reportportal.entity.organization.OrganizationSetting;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Component responsible for extracting and transforming raw organization setting entries into a structured retention
 * policy object used in business logic.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 **/
@Component
public class OrganizationRetentionPolicyHandler {

  private static final String SETTINGS_PREFIX = "retention_";

  /**
   * Converts a map of raw organization settings into a structured {@link OrganizationSettingsRetentionPolicy} object,
   * including launches, logs, and attachments retention periods.
   *
   * @param settings a map of setting keys to {@link OrganizationSetting} objects
   * @return a populated {@link OrganizationSettingsRetentionPolicy} instance
   */
  public OrganizationSettingsRetentionPolicy getRetentionPolicySettings(Map<String, OrganizationSetting> settings) {
    var retentionPolicySettings = settings.entrySet().stream().filter(it -> it.getKey().startsWith(SETTINGS_PREFIX))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    return OrganizationSettingsRetentionPolicy.builder()
        .launches(OrganizationSettingsRetentionPolicyLaunches.builder()
            .period(getRetentionValue(retentionPolicySettings, RETENTION_LAUNCHES.getName())).build())
        .logs(OrganizationSettingsRetentionPolicyLogs.builder()
            .period(getRetentionValue(retentionPolicySettings, RETENTION_LOGS.getName())).build())
        .attachments(OrganizationSettingsRetentionPolicyAttachments.builder()
            .period(getRetentionValue(retentionPolicySettings, RETENTION_ATTACHMENTS.getName())).build())
        .build();
  }

  private int getRetentionValue(Map<String, OrganizationSetting> retentionPolicySettings, String retentionKey) {
    return Integer.parseInt(retentionPolicySettings.get(retentionKey).getSettingValue());
  }

}
