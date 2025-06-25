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

package com.epam.ta.reportportal.core.organization;

import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import com.epam.ta.reportportal.entity.user.User;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for creating personal organizations for users.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Slf4j
@Service
public class PersonalOrganizationService {

  private final Pf4jPluginBox pluginBox;

  /**
   * Constructor for PersonalOrganizationService.
   *
   * @param pluginBox The plugin box to retrieve organization extensions.
   */
  public PersonalOrganizationService(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  /**
   * Creates a personal organization for the given user.
   *
   * @param user The user for whom the personal organization is to be created.
   * @return An Optional containing the OrganizationInfo if creation was successful, or empty if it failed.
   */
  public Optional<OrganizationInfo> create(User user) {
    try {
      return getOrgExtension().map(ext -> ext.createPersonalOrganization(user));
    } catch (IllegalStateException e) {
      log.warn("Can't create personal organization, reason: {}", e.getMessage());
      return Optional.empty();
    } catch (Exception e) {
      log.error("Can't create personal organization for user: {}", user.getLogin(), e);
      return Optional.empty();
    }
  }

  private Optional<OrganizationExtensionPoint> getOrgExtension() {
    return pluginBox.getInstance(OrganizationExtensionPoint.class);
  }

}
