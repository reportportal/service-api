/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.attribute;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.launch.Launch;

/**
 * @author Ivan Kustau
 */
public interface AttributeHandler {
  /**
   * Handles different attribute creation during the start of launch.
   *
   * @param launch Launch that should be handled
   */
  void handleLaunchStart(Launch launch);

  /**
   * Handles different attribute updates during the launch.
   *
   * @param launch Launch that should be handled
   * @param user   User that performs the action
   */
  void handleLaunchUpdate(Launch launch, ReportPortalUser user);
}
