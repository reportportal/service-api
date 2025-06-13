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

import com.epam.reportportal.api.model.OrganizationSettings;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.organization.OrganizationRepositoryCustom;
import com.epam.ta.reportportal.dao.organization.OrganizationSettingsRepository;
import com.epam.ta.reportportal.entity.organization.OrganizationSetting;
import java.util.stream.Collectors;
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

  /**
   * Retrieves the full set of settings for a given organization ID.
   *
   * @param orgId the ID of the organization
   * @return an {@link OrganizationSettings} object populated with relevant configuration
   */
  public OrganizationSettings getOrganizationSettings(Long orgId) {
    organizationRepository.findById(orgId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.ORGANIZATION_NOT_FOUND, orgId));
    var organizationSettings = settingsRepository.findByOrganizationId(orgId).stream()
        .collect(Collectors.toMap(OrganizationSetting::getSettingKey, it -> it));
    return OrganizationSettings.builder()
        .retentionPolicy(organizationRetentionPolicyHandler.getRetentionPolicySettings(organizationSettings)).build();
  }
}
