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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.IS_ADMIN;

import com.epam.reportportal.api.SettingsApi;
import com.epam.reportportal.api.model.AnalyticsSettingsRequest;
import com.epam.reportportal.api.model.SuccessfulUpdate;
import com.epam.reportportal.api.model.UpdateServerSettingsRequest;
import com.epam.reportportal.base.core.settings.ServerSettingsService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for server-wide instance settings. Implements the {@link SettingsApi} interface.
 */
@RequiredArgsConstructor
@RestController
public class GeneratedSettingsController implements SettingsApi {

  private final ServerSettingsService serverSettingsService;

  @Override
  @Transactional(readOnly = true)
  @PreAuthorize(IS_ADMIN)
  public ResponseEntity<Map<String, String>> getServerSettings() {
    return ResponseEntity.ok(serverSettingsService.getServerSettings());
  }

  @Override
  @Transactional
  @PreAuthorize(IS_ADMIN)
  public ResponseEntity<SuccessfulUpdate> updateServerSettings(
      UpdateServerSettingsRequest updateServerSettingsRequest) {
    return ResponseEntity.ok(serverSettingsService.updateServerSettings(updateServerSettingsRequest, getPrincipal()));
  }

  @Override
  @Transactional
  @PreAuthorize(IS_ADMIN)
  public ResponseEntity<SuccessfulUpdate> saveAnalyticsSettings(AnalyticsSettingsRequest analyticsSettingsRequest) {
    return ResponseEntity.ok(serverSettingsService.saveAnalyticsSettings(analyticsSettingsRequest, getPrincipal()));
  }

  @Override
  @Transactional
  @PreAuthorize(IS_ADMIN)
  public ResponseEntity<SuccessfulUpdate> saveAnalyticsSettingsViaPost(
      AnalyticsSettingsRequest analyticsSettingsRequest) {
    return saveAnalyticsSettings(analyticsSettingsRequest);
  }

  private ReportPortalUser getPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
