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

  @Test
  void shouldAnswerRoleExistenceAcrossProjectsAndOrganizations() {
    // user 1 is the editor of its personal project and belongs to its personal organization
    Assertions.assertTrue(projectUserRepository.existsByUser_IdAndProjectRoleIn(1L, Set.of(ProjectRole.EDITOR)));
    Assertions.assertFalse(projectUserRepository.existsByUser_IdAndProjectRoleIn(1L, Set.of()));
    Assertions.assertFalse(projectUserRepository.existsByUser_IdAndProjectRoleIn(-1L, Set.of(ProjectRole.EDITOR)));
    Assertions.assertTrue(organizationUserRepository.existsByUser_IdAndOrganizationRoleIn(1L,
        Set.of(OrganizationRole.MEMBER, OrganizationRole.MANAGER)));
    Assertions.assertFalse(organizationUserRepository.existsByUser_IdAndOrganizationRoleIn(1L, Set.of()));
    Assertions.assertFalse(
        organizationUserRepository.existsByUser_IdAndOrganizationRoleIn(-1L, Set.of(OrganizationRole.MANAGER)));
  }
}
