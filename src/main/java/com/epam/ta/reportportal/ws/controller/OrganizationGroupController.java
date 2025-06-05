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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ORGANIZATION_MANAGER;
import static com.epam.ta.reportportal.util.SecurityContextUtils.getPrincipal;

import com.epam.reportportal.api.OrganizationGroupApi;
import com.epam.reportportal.api.model.CreateGroupRequest;
import com.epam.reportportal.api.model.CreateOrgGroupRequest;
import com.epam.reportportal.api.model.GroupInfo;
import com.epam.reportportal.api.model.GroupPage;
import com.epam.reportportal.api.model.OrgGroupPage;
import com.epam.ta.reportportal.core.group.GroupExtensionPoint;
import com.epam.ta.reportportal.core.plugin.Pf4jPluginBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for handling organization group-related requests.
 *
 * @author <a href="mailto:reingold_shekhtel@epam.com">Reingold Shekhtel</a>
 */
@RestController
public class OrganizationGroupController implements OrganizationGroupApi {

  private final Pf4jPluginBox pluginBox;

  /**
   * Constructor for the {@link OrganizationGroupController} class.
   *
   * @param pluginBox The {@link Pf4jPluginBox} instance used to access plugin extensions.
   */
  @Autowired
  public OrganizationGroupController(Pf4jPluginBox pluginBox) {
    this.pluginBox = pluginBox;
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional
  public ResponseEntity<GroupInfo> createOrgGroup(Long orgId, CreateOrgGroupRequest request) {
    var group = getGroupExtension().createGroup(
        toCreateGroupRequest(request, orgId),
        getPrincipal().getUserId()
    );
    return new ResponseEntity<>(group, HttpStatus.CREATED);
  }

  @Override
  @PreAuthorize(ORGANIZATION_MANAGER)
  @Transactional(readOnly = true)
  public ResponseEntity<OrgGroupPage> getOrgGroups(
      Long orgId,
      Integer offset,
      Integer limit,
      String order,
      String sort
  ) {
    var groupPage = getGroupExtension().getGroups(offset, limit, order, sort, orgId);
    return ResponseEntity.ok(toOrgGroupPage(groupPage));
  }

  private GroupExtensionPoint getGroupExtension() {
    return pluginBox.getInstance(GroupExtensionPoint.class)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.PAYMENT_REQUIRED,
            "Group management is not available. Please install the 'group' plugin."
        ));
  }

  private CreateGroupRequest toCreateGroupRequest(CreateOrgGroupRequest request, Long orgId) {
    return CreateGroupRequest.builder()
        .name(request.getName())
        .slug(request.getSlug())
        .orgId(orgId)
        .build();
  }

  private OrgGroupPage toOrgGroupPage(GroupPage groupPage) {
    return OrgGroupPage.builder()
        .items(groupPage.getItems())
        .offset(groupPage.getOffset())
        .limit(groupPage.getLimit())
        .totalCount(groupPage.getTotalCount())
        .sort(groupPage.getSort())
        .order(groupPage.getOrder())
        .build();
  }
}
