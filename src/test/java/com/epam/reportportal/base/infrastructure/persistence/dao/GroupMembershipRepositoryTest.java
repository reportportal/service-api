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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupProject;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test class for @{@link GroupMembershipRepository}.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Sql("/db/fill/group/group-fill.sql")
class GroupMembershipRepositoryTest extends BaseMvcTest {

  @Autowired
  private CacheManager cacheManager;
  @Autowired
  private GroupMembershipRepository groupMembershipRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ProjectRepository projectRepository;

  private User chubaka;
  private User fakeChubaka;
  private Project falcon;

  @BeforeEach
  void setUp() {
    chubaka = userRepository.findByLogin("chubaka")
        .orElseThrow(() -> new RuntimeException("User not found"));
    fakeChubaka = userRepository.findByLogin("fake_chubaka")
        .orElseThrow(() -> new RuntimeException("User not found"));
    falcon = projectRepository.findByName("millennium_falcon")
        .orElseThrow(() -> new RuntimeException("Project not found"));
  }

  @Test
  void shouldReturnUserGroupProjectRoles() {
    final List<ProjectRole> projectRoles = groupMembershipRepository.findUserProjectRoles(
        chubaka.getId(),
        falcon.getId()
    );
    assertEquals(2, projectRoles.size());
  }

  @Test
  void shouldGetMaxUserProjectRole() {
    final List<ProjectRole> projectRoles = groupMembershipRepository.findUserProjectRoles(
        chubaka.getId(),
        falcon.getId()
    );

    var membershipDetails = MembershipDetails.builder()
        .withProjectHighestRole(projectRoles)
        .build();

    assertEquals(ProjectRole.EDITOR, membershipDetails.getProjectRole());
  }

  @Test
  void shouldReturnMembershipDetails() {
    final var membershipDetails = groupMembershipRepository
        .findMembershipDetails(chubaka.getId(), falcon.getName())
        .orElseThrow(() -> new RuntimeException("Membership details not found"));

    assertEquals(falcon.getId(), membershipDetails.getProjectId());
    assertEquals(ProjectRole.EDITOR, membershipDetails.getProjectRole());
  }

  @Test
  void shouldReturnMembershipDetailsByProjectId() {

    final var membershipDetails = groupMembershipRepository
        .findMembershipDetails(chubaka.getId(), falcon.getId())
        .orElseThrow(() -> new RuntimeException("Membership details not found"));

    assertEquals(falcon.getId(), membershipDetails.getProjectId());
    assertEquals(ProjectRole.EDITOR, membershipDetails.getProjectRole());
  }

  @Test
  void ShouldCacheUserProjectRoles() {
    final Long userId = chubaka.getId();
    final Long projectId = falcon.getId();

    final List<ProjectRole> projectRoles = groupMembershipRepository.findUserProjectRoles(
        userId,
        projectId
    );
    assertNotNull(projectRoles);

    Cache cache = cacheManager.getCache("groupUserProjectRolesCache");
    assertNotNull(cache);

    Cache.ValueWrapper valueWrapper = cache.get(userId + "_" + projectId);
    assertNotNull(valueWrapper);
  }

  @Test
  void ShouldCacheMembershipDetails() {
    final Long userId = chubaka.getId();
    final String projectName = falcon.getName();
    groupMembershipRepository.findMembershipDetails(userId, projectName)
        .orElseThrow(() -> new RuntimeException("Membership details not found"));

    Cache cache = cacheManager.getCache("groupProjectDetailsCache");
    assertNotNull(cache);
    Cache.ValueWrapper valueWrapper = cache.get(userId + "_" + projectName);
    assertNotNull(valueWrapper);
  }

  @Test
  void ShouldReturnAllUserProjects() {
    List<GroupProject> groupProjects = groupMembershipRepository.findAllUserProjects(
        fakeChubaka.getId());
    assertEquals(4, groupProjects.size());
  }

  @Test
  void ShouldReturnAllUserProjectsInOrganization() {
    var groupProjects = groupMembershipRepository.findAllUserProjectsInOrganization(
        fakeChubaka.getId(),
        1L
    );
    assertEquals(4, groupProjects.size());
  }
}
