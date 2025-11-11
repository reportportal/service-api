package com.epam.reportportal.infrastructure.persistence.entity.organization;

import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MembershipDetails {

  private Long orgId;
  private String orgName;
  private OrganizationRole orgRole;
  private Long projectId;
  private String projectName;
  private String projectKey;
  private String projectSlug;
  private ProjectRole projectRole;

  public static MembershipDetailsBuilder builder() {
    return new MembershipDetailsBuilder();
  }

  public static class MembershipDetailsBuilder {

    private Long orgId;
    private String orgName;
    private OrganizationRole orgRole;
    private Long projectId;
    private String projectName;
    private String projectKey;
    private String projectSlug;
    private ProjectRole projectRole;

    public MembershipDetailsBuilder() {
    }

    public MembershipDetailsBuilder withOrgId(Long orgId) {
      this.orgId = orgId;
      return this;
    }

    public MembershipDetailsBuilder withOrgName(String orgName) {
      this.orgName = orgName;
      return this;
    }

    public MembershipDetailsBuilder withOrgRole(OrganizationRole orgRole) {
      this.orgRole = orgRole;
      return this;
    }

    public MembershipDetailsBuilder withProjectId(Long projectId) {
      this.projectId = projectId;
      return this;
    }

    public MembershipDetailsBuilder withProjectName(String projectName) {
      this.projectName = projectName;
      return this;
    }

    public MembershipDetailsBuilder withProjectKey(String projectKey) {
      this.projectKey = projectKey;
      return this;
    }

    public MembershipDetailsBuilder withProjectSlug(String projectSlug) {
      this.projectSlug = projectSlug;
      return this;
    }

    public MembershipDetailsBuilder withProjectRole(ProjectRole projectRole) {
      this.projectRole = projectRole;
      return this;
    }

    public MembershipDetailsBuilder withProjectHighestRole(Collection<ProjectRole> roles) {
      this.projectRole = new ArrayList<>(roles).stream()
          .max(ProjectRole::compareTo)
          .orElse(null);
      return this;
    }

    public MembershipDetailsBuilder withProjectHighestRole(String[] roles) {
      var projectRoles = Arrays.stream(roles)
          .map(ProjectRole::valueOf)
          .toList();
      this.withProjectHighestRole(projectRoles);
      return this;
    }

    public MembershipDetails build() {
      return new MembershipDetails(orgId, orgName, orgRole, projectId, projectName, projectKey,
          projectSlug, projectRole);
    }
  }


}
