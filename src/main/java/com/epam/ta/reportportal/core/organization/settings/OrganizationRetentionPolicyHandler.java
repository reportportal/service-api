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
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.organization.OrganizationSetting;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Component responsible for extracting and transforming raw organization setting entries into a structured retention
 * policy object used in business logic.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 **/
@Component
public class OrganizationRetentionPolicyHandler {

  /**
   * Converts a map of raw organization settings into a structured {@link OrganizationSettingsRetentionPolicy} object,
   * including launches, logs, and attachments retention periods.
   *
   * @param settings a list of {@link OrganizationSetting} objects
   * @return a populated {@link OrganizationSettingsRetentionPolicy} instance
   */
  public OrganizationSettingsRetentionPolicy getRetentionPolicySettings(List<OrganizationSetting> settings) {
    return OrganizationSettingsRetentionPolicy.builder()
        .launches(OrganizationSettingsRetentionPolicyLaunches.builder()
            .period(getRetentionValue(settings, RETENTION_LAUNCHES)).build())
        .logs(OrganizationSettingsRetentionPolicyLogs.builder()
            .period(getRetentionValue(settings, RETENTION_LOGS)).build())
        .attachments(OrganizationSettingsRetentionPolicyAttachments.builder()
            .period(getRetentionValue(settings, RETENTION_ATTACHMENTS)).build())
        .build();
  }

  public int getRetentionValue(List<OrganizationSetting> retentionPolicySettings,
      OrganizationSettingsEnum retentionKey) {
    return Integer.parseInt(
        retentionPolicySettings.stream().filter(it -> retentionKey.getName().equalsIgnoreCase(it.getSettingKey()))
            .findFirst().orElseThrow(() -> new ReportPortalException("Incorrect retention key: " + retentionKey))
            .getSettingValue());
  }

  public OrganizationSetting getOrganizationSetting(List<OrganizationSetting> retentionPolicySettings,
      OrganizationSettingsEnum retentionKey) {
    return retentionPolicySettings.stream().filter(it -> retentionKey.getName().equalsIgnoreCase(it.getSettingKey()))
        .findFirst().orElseThrow(() -> new ReportPortalException("Incorrect retention key: " + retentionKey));
  }

  /**
   * Validates that retention values follow the rule: launches ≥ logs ≥ attachments where 0 means "forever" and is
   * considered greater than any positive number.
   */
  public boolean isRetentionOrderValid(int launches, int logs, int attachments) {
    return compareWithUnlimitedAsMax(launches, logs) >= 0 && compareWithUnlimitedAsMax(logs, attachments) >= 0;
  }

  /**
   * Compares two retention values considering that 0 = "forever" = highest value.
   *
   * @return positive if first ≥ second, negative if not
   */
  private int compareWithUnlimitedAsMax(int first, int second) {
    first = first == 0 ? Integer.MAX_VALUE : first;
    second = second == 0 ? Integer.MAX_VALUE : second;
    return Integer.compare(first, second);
  }

}
