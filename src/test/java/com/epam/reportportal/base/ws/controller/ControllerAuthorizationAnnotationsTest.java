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
import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.model.BulkItemsRQ;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Guards the method-level authorization of endpoints that must not be open to project-viewer callers.
 */
class ControllerAuthorizationAnnotationsTest {

  @Test
  void executePluginCommandRequiresProjectEditPermission() throws Exception {
    PreAuthorize preAuthorize = PluginController.class
        .getMethod("executePluginCommand", String.class, String.class, String.class, Map.class,
            ReportPortalUser.class)
        .getAnnotation(PreAuthorize.class);

    assertEquals(ALLOWED_TO_EDIT_PROJECT, preAuthorize.value());
  }

  @Test
  void readEndpointsRequireProjectViewPermission() throws Exception {
    List<Method> methods = List.of(
        LaunchController.class.getMethod("getLaunchByUuidOldTimestamp", String.class, String.class,
            ReportPortalUser.class),
        TestItemController.class.getMethod("getTestItemByUuidTimestamp", String.class,
            ReportPortalUser.class, String.class),
        TestItemController.class.getMethod("getAttributeValues", String.class,
            ReportPortalUser.class, Long.class, String.class, String.class),
        TestItemController.class.getMethod("getTestItemsByIds", String.class,
            ReportPortalUser.class, BulkItemsRQ.class),
        LogController.class.getMethod("getLogsWithLocationBySearch", String.class, Long.class,
            Map.class, Filter.class, Pageable.class, ReportPortalUser.class));

    for (Method method : methods) {
      PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
      assertNotNull(preAuthorize, method.getName());
      assertEquals(ALLOWED_TO_VIEW_PROJECT, preAuthorize.value(), method.getName());
    }
  }
}
