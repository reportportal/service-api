/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_EDIT_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Guards the method-level authorization of endpoints that must not be open to every authenticated or project-viewer
 * caller.
 */
class ControllerAuthorizationAnnotationsTest {

  @Test
  void getUserProjectsIsAllowedOnlyToTheUserOrAdministrator() throws Exception {
    PreAuthorize preAuthorize = UserController.class
        .getMethod("getUserProjects", String.class, ReportPortalUser.class)
        .getAnnotation(PreAuthorize.class);

    assertEquals(ALLOWED_TO_EDIT_USER, preAuthorize.value());
  }

  @Test
  void executePluginCommandRequiresProjectEditPermission() throws Exception {
    PreAuthorize preAuthorize = PluginController.class
        .getMethod("executePluginCommand", String.class, String.class, String.class, Map.class,
            ReportPortalUser.class)
        .getAnnotation(PreAuthorize.class);

    assertEquals(ALLOWED_TO_EDIT_PROJECT, preAuthorize.value());
  }
}
