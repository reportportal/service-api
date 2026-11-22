package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;

import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

class ProjectUserRepositoryTest extends BaseMvcTest {

  @Autowired
  private ProjectUserRepository projectUserRepository;

  @Autowired
  private OrganizationUserRepository organizationUserRepository;


  @Test
  void shouldFindDetailsByUserIdAndProjectKey() {

    final String projectKey = "superadmin_personal";
    final Optional<MembershipDetails> membershipDetails =
        projectUserRepository.findDetailsByUserIdAndProjectKey(1L, projectKey);

    Assertions.assertTrue(membershipDetails.isPresent());
    Assertions.assertNotNull(membershipDetails.get().getOrgId());

    Assertions.assertEquals(projectKey, membershipDetails.get().getProjectName());
    Assertions.assertEquals(1L, membershipDetails.get().getProjectId());
    Assertions.assertEquals(ProjectRole.EDITOR, membershipDetails.get().getProjectRole());
  }

  @Test
  void shouldNotFindDetailsByUserIdAndProjectKeyWhenNotExists() {

    final String projectKey = "falcon-key";
    final Optional<MembershipDetails> projectDetails =
        projectUserRepository.findDetailsByUserIdAndProjectKey(2L, projectKey);

    Assertions.assertFalse(projectDetails.isPresent());
  }

  @Test
  void findUserProjectsInOrganization() {

    Pageable pageable = PageRequest.of(0, 50, Sort.by(Sort.Order.by(CRITERIA_NAME)));
    final Page<MembershipDetails> memberDetails =
        projectUserRepository.findUserProjectsInOrganization(1L, 1L, pageable);

    Assertions.assertEquals(1, memberDetails.getNumberOfElements());
  }

  @Test
  void findUserProjectIdsInOrganization() {

    final Set<Long> projectIds =
        projectUserRepository.findUserProjectIdsInOrganization(1L, 1L);

    Assertions.assertEquals(1, projectIds.size());
  }

  private static final Set<ProjectRole> EDITOR_UP = Set.of(ProjectRole.EDITOR);
  private static final Set<OrganizationRole> MANAGER_ONLY = Set.of(OrganizationRole.MANAGER);

  private boolean projectRole(long userId) {
    return projectUserRepository.existsRoleInNonPersonalOrganizationProject(userId, EDITOR_UP);
  }

  private boolean orgRole(long userId) {
    return organizationUserRepository.existsRoleInNonPersonalOrganization(userId, MANAGER_ONLY);
  }

  @Test
  void shouldCountDefaultOrganizationAndRejectUnknownUsersAndEmptyRoles() {
    // the migrated default users live in the default organization, which is not of type PERSONAL
    Assertions.assertTrue(projectRole(1L));
    Assertions.assertTrue(organizationUserRepository.existsRoleInNonPersonalOrganization(1L,
        Set.of(OrganizationRole.MEMBER, OrganizationRole.MANAGER)));
    Assertions.assertFalse(projectRole(-1L));
    Assertions.assertFalse(orgRole(-1L));
    Assertions.assertFalse(projectUserRepository.existsRoleInNonPersonalOrganizationProject(1L, Set.of()));
  }

  @Test
  @Sql("/db/fill/personal/personal-organization-roles-fill.sql")
  void shouldCountOnlyRolesInNonPersonalOrganizations() {
    // 701: EDITOR in a personal project only; 702: MANAGER of a personal organization only
    Assertions.assertFalse(projectRole(701L));
    Assertions.assertFalse(orgRole(701L));
    Assertions.assertFalse(projectRole(702L));
    Assertions.assertFalse(orgRole(702L));
    // 703: EDITOR in a project of a regular organization
    Assertions.assertTrue(projectRole(703L));
    Assertions.assertFalse(orgRole(703L));
    // 704: MANAGER of a regular organization
    Assertions.assertTrue(orgRole(704L));
    Assertions.assertFalse(projectRole(704L));
    // 705: both kinds of roles
    Assertions.assertTrue(projectRole(705L));
    Assertions.assertTrue(orgRole(705L));
    // the role filter still applies to regular organizations
    Assertions.assertFalse(projectUserRepository.existsRoleInNonPersonalOrganizationProject(703L,
        Set.of(ProjectRole.VIEWER)));
  }
}
