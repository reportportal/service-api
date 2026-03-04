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

package com.epam.reportportal.base.infrastructure.persistence.commons;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser.OrganizationDetails.ProjectDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.ProjectUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * ReportPortal user representation
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ReportPortalUser extends User {

  private boolean active;

  private Long userId;
  private UserRole userRole;
  private String email;
  private Map<String, OrganizationDetails> organizationDetails;


  private ReportPortalUser(String username, String password,
      Collection<? extends GrantedAuthority> authorities, Long userId,
      UserRole role, Map<String, OrganizationDetails> organizationDetails, String email, boolean isActive) {
    super(username, password, authorities);
    this.userId = userId;
    this.userRole = role;
    this.organizationDetails = organizationDetails;
    this.email = email;
    this.active = isActive;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }

  @Override
  public boolean isAccountNonLocked() {
    return active;
  }


  public static ReportPortalUserBuilder userBuilder() {
    return new ReportPortalUserBuilder();
  }


  @Getter
  @Setter
  @AllArgsConstructor
  public static class OrganizationDetails implements Serializable {

    @JsonProperty(value = "id")
    private Long orgId;

    @JsonProperty(value = "name")
    private String orgName;

    @JsonProperty("role")
    private OrganizationRole orgRole;

    private Map<String, ProjectDetails> projectDetails;

    public static OrganizationDetailsBuilder builder() {
      return new OrganizationDetailsBuilder();
    }

    public static class OrganizationDetailsBuilder {

      private Long orgId;
      private String orgName;
      private OrganizationRole orgRole;
      private Map<String, ProjectDetails> projectDetails;


      private OrganizationDetailsBuilder() {
      }

      public OrganizationDetailsBuilder withOrgId(Long orgId) {
        this.orgId = orgId;
        return this;
      }

      public OrganizationDetailsBuilder withOrgName(String orgName) {
        this.orgName = orgName;
        return this;
      }

      public OrganizationDetailsBuilder withOrganizationRole(String orgRole) {
        this.orgRole = OrganizationRole.forName(orgRole)
            .orElseThrow(() -> new ReportPortalException(ErrorType.ROLE_NOT_FOUND, orgRole));
        return this;
      }

      public OrganizationDetailsBuilder withProjectDetails(Map<String, ProjectDetails> projectDetails) {
        this.projectDetails = projectDetails;
        return this;
      }

      public OrganizationDetails build() {
        return new OrganizationDetails(orgId, orgName, orgRole, projectDetails);
      }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDetails implements Serializable {

      @JsonProperty(value = "id")
      private Long projectId;

      @JsonProperty(value = "name")
      private String projectName;

      @JsonProperty(value = "key")
      private String projectKey;

      @JsonProperty("role")
      private ProjectRole projectRole;

      @JsonProperty("organization_id")
      private Long organizationId;


      public static ProjectDetailsBuilder builder() {
        return new ProjectDetailsBuilder();
      }

      public static class ProjectDetailsBuilder {

        private Long projectId;
        private String projectName;
        private String projectKey;
        private ProjectRole projectRole;
        private Long organizationId;

        private ProjectDetailsBuilder() {
        }

        public ProjectDetailsBuilder withProjectId(Long projectId) {
          this.projectId = projectId;
          return this;
        }

        public ProjectDetailsBuilder withProjectName(String projectName) {
          this.projectName = projectName;
          return this;
        }

        public ProjectDetailsBuilder withProjectKey(String projectKey) {
          this.projectKey = projectKey;
          return this;
        }

        public ProjectDetailsBuilder withOrgId(Long orgId) {
          this.organizationId = orgId;
          return this;
        }

        public ProjectDetailsBuilder withProjectRole(String projectRole) {
          this.projectRole = ProjectRole.forName(projectRole)
              .orElseThrow(() -> new ReportPortalException(ErrorType.ROLE_NOT_FOUND, projectRole));
          return this;
        }

        public ProjectDetails build() {
          return new ProjectDetails(projectId, projectName, projectKey, projectRole, organizationId);
        }
      }
    }

  }

  public static class ReportPortalUserBuilder {

    private boolean active;
    private String username;
    private String password;
    private Long userId;
    private UserRole userRole;
    private String email;
    private Map<String, OrganizationDetails> organizationDetails;
    private Collection<? extends GrantedAuthority> authorities;

    private ReportPortalUserBuilder() {
    }

    public ReportPortalUserBuilder withActive(boolean active) {
      this.active = active;
      return this;
    }

    public ReportPortalUserBuilder withUserName(String userName) {
      this.username = userName;
      return this;
    }

    public ReportPortalUserBuilder withPassword(String password) {
      this.password = password;
      return this;
    }

    public ReportPortalUserBuilder withAuthorities(
        Collection<? extends GrantedAuthority> authorities) {
      this.authorities = authorities;
      return this;
    }

    public ReportPortalUserBuilder withUserDetails(UserDetails userDetails) {
      this.username = userDetails.getUsername();
      this.password = userDetails.getPassword();
      this.authorities = userDetails.getAuthorities();
      this.active = userDetails.isEnabled();
      return this;
    }

    public ReportPortalUserBuilder withUserId(Long userId) {
      this.userId = userId;
      return this;
    }

    public ReportPortalUserBuilder withUserRole(UserRole userRole) {
      this.userRole = userRole;
      return this;
    }

    public ReportPortalUserBuilder withEmail(String email) {
      this.email = email;
      return this;
    }

    public ReportPortalUserBuilder withOrganizationDetails(Map<String, OrganizationDetails> organizationDetails) {
      this.organizationDetails = organizationDetails;
      return this;
    }

    public ReportPortalUser fromUser(com.epam.reportportal.base.infrastructure.persistence.entity.user.User user) {
      this.active = user.getActive();
      this.username = user.getLogin();
      this.email = user.getPassword();
      this.userId = user.getId();
      this.userRole = user.getRole();
      this.password = ofNullable(user.getPassword()).orElse("");
      this.authorities = Collections.singletonList(
          new SimpleGrantedAuthority(user.getRole().getAuthority()));
      this.organizationDetails = user.getOrganizationUsers()
          .stream()
          .collect(Collectors.toMap(it -> it.getOrganization().getName(),
              it -> OrganizationDetails.builder()
                  .withOrgId(it.getOrganization().getId())
                  .withOrganizationRole(it.getOrganizationRole().name())
                  .withProjectDetails(mapProjectDetails(user.getProjects(), it.getOrganization().getId()))
                  .withOrgName(it.getOrganization().getName())
                  .build()
          ));
      return build();
    }

    private Map<String, ProjectDetails> mapProjectDetails(Set<ProjectUser> projects, Long orgId) {
      return projects.stream()
          .filter(projectUser -> projectUser.getProject().getOrganizationId().equals(orgId))
          .collect(Collectors.toMap(projectUser -> projectUser.getProject().getKey(),
              projectUser -> ProjectDetails.builder()
                  .withProjectId(projectUser.getProject().getId())
                  .withProjectRole(projectUser.getProjectRole().name())
                  .withProjectKey(projectUser.getProject().getKey())
                  .build())
          );
    }

    public ReportPortalUser build() {
      return new ReportPortalUser(username, password, authorities, userId, userRole,
          organizationDetails, email, active);
    }
  }
}
