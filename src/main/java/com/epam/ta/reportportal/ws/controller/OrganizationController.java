/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.organization.GetOrganizationHandler;
import com.epam.ta.reportportal.model.OrganizationResource;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Andrei Piankouski
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

  private final GetOrganizationHandler getOrganizationHandler;

  @Autowired
  public OrganizationController(GetOrganizationHandler getOrganizationHandler) {
    this.getOrganizationHandler = getOrganizationHandler;
  }

  @Transactional
  @GetMapping("/{organizationId}")
  @ApiOperation(value = "Get information about organization")
  public OrganizationResource getOrganization(@PathVariable Long organizationId,
      @AuthenticationPrincipal ReportPortalUser user) {
    return getOrganizationHandler.getResource(organizationId, user);
  }

  @Transactional
  @GetMapping("/list")
  @ApiOperation(value = "Get list of all organizations")
  public List<OrganizationResource> getAllOrganizations(
      @AuthenticationPrincipal ReportPortalUser user) {
    return getOrganizationHandler.getAllOrganization();
  }
}