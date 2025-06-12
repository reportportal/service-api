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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.AccountType;
import com.epam.reportportal.api.model.InstanceRole;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.OrgType;
import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationStatsRelationships;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsLaunches;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsLaunchesMeta;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsProjects;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsProjectsMeta;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsUsers;
import com.epam.reportportal.api.model.OrganizationStatsRelationshipsUsersMeta;
import com.epam.reportportal.api.model.OrganizationUser;
import com.epam.reportportal.api.model.OrganizationUserAllOfStats;
import com.epam.reportportal.api.model.OrganizationUserAllOfStatsProjectStats;
import com.epam.reportportal.api.model.ProjectInfo;
import com.epam.reportportal.api.model.ProjectStats;
import com.epam.reportportal.api.model.ProjectStatsLaunchStats;
import com.epam.reportportal.api.model.ProjectStatsUserStats;
import com.epam.ta.reportportal.entity.organization.OrganizationProfile;
import com.epam.ta.reportportal.entity.organization.OrganizationUserAccount;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectProfile;
import java.util.function.Function;

/**
 * Converts project entity into OrganizationProjectInfo api model.
 *
 * @author Siarhei Hrabko
 */
public final class OrganizationConverter {

  private OrganizationConverter() {
    //static only
  }

  public static Function<Project, ProjectInfo> PROJECT_TO_ORG_PROJECT_INFO = project -> {
    if (project == null) {
      return null;
    }
    ProjectInfo projectInfo = new ProjectInfo();

    projectInfo.setId(project.getId());
    projectInfo.setName(project.getName());
    projectInfo.setSlug(project.getSlug());
    projectInfo.setKey(project.getKey());
    projectInfo.setOrganizationId(project.getOrganizationId());
    projectInfo.setCreatedAt(project.getCreationDate());
    projectInfo.setUpdatedAt(project.getUpdatedAt());
    return projectInfo;
  };


  public static Function<ProjectProfile, ProjectInfo> PROJECT_PROFILE_TO_ORG_PROJECT_INFO = project -> {
    if (project == null) {
      return null;
    }
    ProjectInfo projectInfo = new ProjectInfo();

    projectInfo.setId(project.getId());
    projectInfo.setName(project.getName());
    projectInfo.setSlug(project.getSlug());
    projectInfo.setKey(project.getKey());
    projectInfo.setOrganizationId(project.getOrganizationId());
    projectInfo.setCreatedAt(project.getCreatedAt());
    projectInfo.setUpdatedAt(project.getUpdatedAt());
    projectInfo.stats(new ProjectStats()
        .userStats(new ProjectStatsUserStats()
            .totalCount(project.getUsersQuantity()))
        .launchStats(new ProjectStatsLaunchStats()
            .totalCount(project.getLaunchesQuantity())
            .lastOccurredAt(project.getLastRun())));
    return projectInfo;
  };


  public static Function<OrganizationUserAccount, OrganizationUser> ORG_USER_ACCOUNT_TO_ORG_USER =
      orgUserAccount -> new OrganizationUser()
          .id(orgUserAccount.getId())
          .fullName(orgUserAccount.getFullName())
          .createdAt(orgUserAccount.getCreatedAt())
          .updatedAt(orgUserAccount.getUpdatedAt())
          .instanceRole(InstanceRole.fromValue(orgUserAccount.getInstanceRole().toString()))
          .orgRole(OrgRole.fromValue(orgUserAccount.getOrgRole().toString()))
          .accountType(AccountType.fromValue(orgUserAccount.getAuthProvider().toString()))
          .email(orgUserAccount.getEmail())
          .lastLoginAt(orgUserAccount.getLastLoginAt())
          .externalId(orgUserAccount.getExternalId())
          .uuid(orgUserAccount.getUuid())
          .stats(new OrganizationUserAllOfStats()
              .projectStats(new OrganizationUserAllOfStatsProjectStats()
                  .totalCount(orgUserAccount.getProjectCount()))
          );

  public static Function<OrganizationProfile, OrganizationInfo> ORG_PROFILE_TO_ORG_INFO =
      orgProfile -> new OrganizationInfo()
          .id(orgProfile.getId())
          .type(OrgType.fromValue(orgProfile.getType()))
          .name(orgProfile.getName())
          .slug(orgProfile.getSlug())
          .createdAt(orgProfile.getCreatedAt())
          .updatedAt(orgProfile.getUpdatedAt())
          .externalId(orgProfile.getExternalId())
          .relationships(new OrganizationStatsRelationships()
              .launches(new OrganizationStatsRelationshipsLaunches()
                  .meta(new OrganizationStatsRelationshipsLaunchesMeta()
                      .count(orgProfile.getLaunchesQuantity())
                      .lastOccurredAt(orgProfile.getLastRun())))
              .projects(new OrganizationStatsRelationshipsProjects()
                  .meta(new OrganizationStatsRelationshipsProjectsMeta()
                      .count(orgProfile.getProjectsQuantity())))
              .users(new OrganizationStatsRelationshipsUsers()
                  .meta(new OrganizationStatsRelationshipsUsersMeta().count(
                      orgProfile.getUsersQuantity()))));

}
