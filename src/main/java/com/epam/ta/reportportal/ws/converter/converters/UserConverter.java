/*
 * Copyright 2019 EPAM Systems
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

import static com.epam.ta.reportportal.util.MetadataUtils.getLastLogin;
import static com.epam.ta.reportportal.util.MetadataUtils.getMediaType;

import com.epam.reportportal.api.model.AccountType;
import com.epam.reportportal.api.model.InstanceRole;
import com.epam.reportportal.api.model.InstanceUser;
import com.epam.reportportal.api.model.InstanceUserOrgDetails;
import com.epam.reportportal.api.model.InstanceUserStats;
import com.epam.reportportal.api.model.InstanceUserStatsOrgStats;
import com.epam.reportportal.api.model.Link;
import com.epam.reportportal.api.model.OrgRole;
import com.epam.reportportal.api.model.UserLinksLinks;
import com.epam.ta.reportportal.commons.MoreCollectors;
import com.epam.ta.reportportal.entity.user.OrganizationUser;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserType;
import com.epam.ta.reportportal.model.activity.UserActivityResource;
import com.epam.ta.reportportal.model.user.SearchUserResource;
import com.epam.ta.reportportal.model.user.UserResource;
import com.google.common.collect.Lists;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Converts user from database to resource
 *
 * @author Pavel Bortnik
 */
public final class UserConverter {

  public static final Function<User, UserResource> TO_RESOURCE = user -> {
    UserResource resource = new UserResource();
    resource.setId(user.getId());
    resource.setUuid(user.getUuid());
    resource.setExternalId(user.getExternalId());
    resource.setActive(user.getActive());
    resource.setUserId(user.getLogin());
    resource.setEmail(user.getEmail());
    resource.setPhotoId(user.getAttachment());
    resource.setFullName(user.getFullName());
    resource.setAccountType(user.getUserType().toString());
    resource.setUserRole(user.getRole().toString());
    resource.setLoaded(UserType.UPSA != user.getUserType());
    resource.setMetadata(user.getMetadata().getMetadata());

    if (CollectionUtils.isNotEmpty(user.getProjects())) {
      List<ProjectUser> projects = Lists.newArrayList(user.getProjects());
      projects.sort(Comparator.comparing(compare -> compare.getProject().getName()));
      Map<String, UserResource.AssignedProject> userProjects = user.getProjects().stream()
          .collect(MoreCollectors.toLinkedMap(p -> p.getProject().getKey(), p -> {
            UserResource.AssignedProject assignedProject = new UserResource.AssignedProject();
            assignedProject.setProjectRole(p.getProjectRole().toString());
            assignedProject.setProjectKey(p.getProject().getKey());
            assignedProject.setProjectName(p.getProject().getName());
            assignedProject.setProjectSlug(p.getProject().getSlug());
            assignedProject.setOrganizationId(p.getProject().getOrganizationId());
            return assignedProject;
          }));
      resource.setAssignedProjects(userProjects);
    }

    if (CollectionUtils.isNotEmpty(user.getOrganizationUsers())) {
      List<OrganizationUser> orgUsers = Lists.newArrayList(user.getOrganizationUsers());
      Map<String, UserResource.AssignedOrganization> userOrganization = orgUsers
          .stream()
          .collect(Collectors.toMap(orgUser -> orgUser.getOrganization().getSlug(),
              orgUser -> {
                UserResource.AssignedOrganization assignedOrganization = new UserResource.AssignedOrganization();
                assignedOrganization.setOrganizationId(orgUser.getOrganization().getId());
                assignedOrganization.setOrganizationName(orgUser.getOrganization().getName());
                assignedOrganization.setOrganizationSlug(orgUser.getOrganization().getSlug());
                assignedOrganization.setOrganizationRole(orgUser.getOrganizationRole().name());
                return assignedOrganization;
              }));
      resource.setAssignedOrganizations(userOrganization);
    }

    return resource;
  };
  public static final Function<User, SearchUserResource> TO_SEARCH_RESOURCE = user -> {
    final SearchUserResource resource = new SearchUserResource();
    resource.setId(user.getId());
    resource.setUuid(user.getUuid());
    resource.setExternalId(user.getExternalId());
    resource.setActive(user.getActive());
    resource.setLogin(user.getLogin());
    resource.setEmail(user.getEmail());
    resource.setFullName(user.getFullName());
    return resource;
  };
  public static final BiFunction<User, Long, UserActivityResource> TO_ACTIVITY_RESOURCE =
      (user, projectId) -> {
        UserActivityResource resource = new UserActivityResource();
        resource.setId(user.getId());
        resource.setDefaultProjectId(projectId);
        resource.setFullName(user.getLogin());
        return resource;
      };

  private UserConverter() {
    //static only
  }

  public static final Function<User, InstanceUser> TO_INSTANCE_USER = user ->
      new InstanceUser()
          .id(user.getId())
          .active(user.getActive())
          .createdAt(user.getCreatedAt())
          .updatedAt(user.getUpdatedAt())
          .lastLoginAt(getLastLogin(user.getMetadata()))
          .uuid(user.getUuid())
          .externalId(user.getExternalId())
          .email(user.getEmail())
          .fullName(user.getFullName())
          .accountType(Optional.ofNullable(user.getUserType())
              .map(type -> AccountType.fromValue(type.toString()))
              .orElse(null))
          .instanceRole(Optional.ofNullable(user.getRole())
              .map(role -> InstanceRole.fromValue(role.toString()))
              .orElse(null))
          .links(getLinks(user))
          .organizations(user.getOrganizationUsers()
              .stream()
              .map(orgUser -> new InstanceUserOrgDetails()
                  .id(orgUser.getOrganization().getId())
                  .slug(orgUser.getOrganization().getSlug())
                  .name(orgUser.getOrganization().getName())
                  .orgRole(OrgRole.fromValue(orgUser.getOrganizationRole().getRoleName().toUpperCase())))
              .collect(Collectors.toSet()))
          .stats(new InstanceUserStats()
              .orgStats(new InstanceUserStatsOrgStats()
                  .totalCount(user.getOrganizationUsers().size())));


  @SneakyThrows
  private static UserLinksLinks getLinks(User user) {
    UserLinksLinks links = new UserLinksLinks();
    String mediaType = getMediaType(user.getMetadata());

    links.self(new Link(new URI("/users/" + user.getId()), null, null));

    if (null != user.getAttachmentThumbnail() && StringUtils.isNotEmpty(mediaType)) {
      links.avatar(
          new Link(new URI("/users/" + user.getId() + "/avatar"), mediaType, "User's profile picture"));
      return links;
    }
    return links;
  }

}
