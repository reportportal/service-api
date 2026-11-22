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

package com.epam.reportportal.base.core.group;

import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Checks that groups, projects and users that are about to be linked by the group plugin belong to the same
 * organization. The group entity is reachable only through {@link GroupExtensionPoint}.
 */
@Component
@RequiredArgsConstructor
public class GroupOrganizationBinding {

  private final ProjectRepository projectRepository;
  private final OrganizationUserRepository organizationUserRepository;

  /**
   * Requires the project to belong to the group's organization.
   *
   * @param extension group plugin
   * @param groupId   group id
   * @param projectId project id
   */
  public void requireProjectInGroupOrganization(GroupExtensionPoint extension, Long groupId, Long projectId) {
    var project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_ID_NOT_FOUND, projectId));
    requireSameOrganization(extension, groupId, project);
  }

  /**
   * Requires the project to belong to the group's organization.
   *
   * @param extension  group plugin
   * @param groupId    group id
   * @param projectKey project key
   */
  public void requireProjectInGroupOrganization(GroupExtensionPoint extension, Long groupId, String projectKey) {
    var project = projectRepository.findByKey(projectKey)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_KEY_NOT_FOUND, projectKey));
    requireSameOrganization(extension, groupId, project);
  }

  /**
   * Requires the user to be a member of the group's organization.
   *
   * @param extension group plugin
   * @param groupId   group id
   * @param userId    user id
   */
  public void requireUserInGroupOrganization(GroupExtensionPoint extension, Long groupId, Long userId) {
    var orgId = groupOrganizationId(extension, groupId);
    if (organizationUserRepository.findByUserIdAndOrganization_Id(userId, orgId).isEmpty()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "User %s is not a member of the organization of group %s".formatted(userId, groupId));
    }
  }

  private void requireSameOrganization(GroupExtensionPoint extension, Long groupId, Project project) {
    var orgId = groupOrganizationId(extension, groupId);
    if (!Objects.equals(orgId, project.getOrganizationId())) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Project %s and group %s belong to different organizations".formatted(project.getId(), groupId));
    }
  }

  private Long groupOrganizationId(GroupExtensionPoint extension, Long groupId) {
    var group = extension.getGroupById(groupId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, groupId));
    if (group.getOrgId() == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Organization of group %s cannot be determined".formatted(groupId));
    }
    return group.getOrgId();
  }
}
