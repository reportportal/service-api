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
package com.epam.ta.reportportal.core.settings.handlers;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.organization.OrganizationExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.core.settings.ServerSettingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Handler for the "personal organization" server setting.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalOrganizationSettingHandler implements ServerSettingHandler {

  public static final String PERSONAL_ORGANIZATION_SETTINGS_KEY = "server.features.personal-organization.enabled";

  private final Pf4jPluginBox pluginBox;

  @Override
  public void handle(String value) {
    pluginBox.getInstance(OrganizationExtensionPoint.class)
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.BAD_REQUEST_ERROR,
            "Organization management is not available. Please install the 'organization' plugin."
        ));

    log.info("Personal organization setting is set to '{}'", value);
  }

  @Override
  public String getKey() {
    return PERSONAL_ORGANIZATION_SETTINGS_KEY;
  }
}
