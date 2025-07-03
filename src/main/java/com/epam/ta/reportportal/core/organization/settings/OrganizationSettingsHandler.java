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

import static com.epam.reportportal.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_ATTACHMENTS;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_LAUNCHES;
import static com.epam.ta.reportportal.core.organization.settings.OrganizationSettingsEnum.RETENTION_LOGS;

import com.epam.reportportal.api.model.OrganizationSettings;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyAttachments;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyLaunches;
import com.epam.reportportal.api.model.OrganizationSettingsRetentionPolicyLogs;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationSettingsRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationSetting;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for retrieving and assembling organization-wide settings.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 **/
@Service
@RequiredArgsConstructor
public class OrganizationSettingsHandler {

  private final OrganizationRepositoryCustom organizationRepository;
  private final OrganizationSettingsRepository settingsRepository;
  private final OrganizationRetentionPolicyHandler organizationRetentionPolicyHandler;
  private final ProjectRepository projectRepository;

  /**
   * Retrieves the full set of settings for a given organization ID.
   *
   * @param orgId the ID of the organization
   * @return an {@link OrganizationSettings} object populated with relevant configuration
   */
  public OrganizationSettings getOrganizationSettings(Long orgId) {
    organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
    var organizationSettings = settingsRepository.findByOrganizationId(orgId);
    return OrganizationSettings.builder()
        .retentionPolicy(organizationRetentionPolicyHandler.getRetentionPolicySettings(organizationSettings)).build();
  }

  public void updateOrgSettings(Long orgId, OrganizationSettings updateSettings) {
    organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));

    var currentSettings = settingsRepository.findByOrganizationId(orgId);

    var currentLaunchesPeriod = organizationRetentionPolicyHandler.getRetentionValue(currentSettings,
        RETENTION_LAUNCHES);
    var currentLogsPeriod = organizationRetentionPolicyHandler.getRetentionValue(currentSettings,
        RETENTION_LOGS);
    var currentAttachmentsPeriod = organizationRetentionPolicyHandler.getRetentionValue(currentSettings,
        RETENTION_ATTACHMENTS);

    if (updateSettings.getRetentionPolicy() != null) {
      //Replace it with a separate service if new settings appeared.
      var retentionPolicies = updateSettings.getRetentionPolicy();
      var updatedLaunchesPeriod = Optional.of(retentionPolicies.getLaunches())
          .map(OrganizationSettingsRetentionPolicyLaunches::getPeriod).orElse(currentLaunchesPeriod);
      var updatedLogsPeriod = Optional.of(retentionPolicies.getLogs())
          .map(OrganizationSettingsRetentionPolicyLogs::getPeriod).orElse(currentLogsPeriod);
      var updatedAttachmentsPeriod = Optional.of(retentionPolicies.getAttachments())
          .map(OrganizationSettingsRetentionPolicyAttachments::getPeriod).orElse(currentAttachmentsPeriod);

      if (!organizationRetentionPolicyHandler.isRetentionOrderValid(updatedLaunchesPeriod, updatedLogsPeriod,
          updatedAttachmentsPeriod)) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Retention periods must follow: launches >= logs >= attachments");
      }

      updateRetentionSettingsValue(currentSettings, RETENTION_LAUNCHES, updatedLaunchesPeriod, orgId);
      updateRetentionSettingsValue(currentSettings, RETENTION_LOGS, updatedLogsPeriod, orgId);
      updateRetentionSettingsValue(currentSettings, RETENTION_ATTACHMENTS, updatedAttachmentsPeriod, orgId);

    }
  }

  private void updateRetentionSettingsValue(List<OrganizationSetting> settings,
      OrganizationSettingsEnum settingsEnum, Integer updateValue, long orgId) {
    var organizationSetting = organizationRetentionPolicyHandler.getOrganizationSetting(settings,
        settingsEnum);
    organizationSetting.setSettingValue(String.valueOf(updateValue));
    settingsRepository.save(organizationSetting);
    if (updateValue != 0) {
      projectRepository.updateProjectAttributeValueIfGreater(TimeUnit.DAYS.toSeconds(updateValue),
          settingsEnum.getProjectFormatKey(), orgId);
    }
  }
}
