/*
 * Copyright 2026 EPAM Systems
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

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.reportportal.base.infrastructure.persistence.entity.group.Group;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupProjectId;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupUserId;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.ws.BaseMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.jdbc.Sql;

/**
 * Group membership and group role changes must drop the cached group-derived project roles at once (not after the
 * 30 second expiry). Uses the real cache manager and the real repositories.
 */
@Sql("/db/fill/group/group-fill.sql")
class GroupAccessCacheEvictionTest extends BaseMvcTest {

  @Autowired
  @Qualifier("caffeineCacheManager")
  private CacheManager cacheManager;
  @Autowired
  private GroupMembershipRepository membershipRepository;
  @Autowired
  private GroupUserRepository groupUserRepository;
  @Autowired
  private GroupProjectRepository groupProjectRepository;
  @Autowired
  private GroupRepository groupRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ProjectRepository projectRepository;

  private User chubaka;
  private Project falcon;
  private Group rebel;
  private Group ewoks;

  @BeforeEach
  void setUp() {
    chubaka = userRepository.findByLogin("chubaka").orElseThrow();
    falcon = projectRepository.findByName("millennium_falcon").orElseThrow();
    rebel = groupRepository.findBySlug("rebel-group").orElseThrow();
    ewoks = groupRepository.findBySlug("ewoks-group").orElseThrow();
  }

  /** chubaka is EDITOR (rebel) and VIEWER (ewoks) in falcon; fills both caches under the keys used by the readers. */
  private void warmCaches() {
    assertThat(membershipRepository.findUserProjectRoles(chubaka.getId(), falcon.getId()))
        .containsExactlyInAnyOrder(ProjectRole.EDITOR, ProjectRole.VIEWER);
    assertThat(membershipRepository.findMembershipDetails(chubaka.getId(), falcon.getName()).orElseThrow()
        .getProjectRole()).isEqualTo(ProjectRole.EDITOR);
    assertThat(membershipRepository.findMembershipDetails(chubaka.getId(), falcon.getId()).orElseThrow()
        .getProjectRole()).isEqualTo(ProjectRole.EDITOR);
    assertThat(rolesCache().get(chubaka.getId() + "_" + falcon.getId())).isNotNull();
    assertThat(detailsCache().get(chubaka.getId() + "_" + falcon.getName())).isNotNull();
    assertThat(detailsCache().get(chubaka.getId() + "_" + falcon.getId())).isNotNull();
  }

  private void assertCachesEmpty() {
    assertThat(rolesCache().get(chubaka.getId() + "_" + falcon.getId())).isNull();
    assertThat(detailsCache().get(chubaka.getId() + "_" + falcon.getName())).isNull();
    assertThat(detailsCache().get(chubaka.getId() + "_" + falcon.getId())).isNull();
  }

  private Cache rolesCache() {
    return cacheManager.getCache("groupUserProjectRolesCache");
  }

  private Cache detailsCache() {
    return cacheManager.getCache("groupProjectDetailsCache");
  }

  @Test
  void removingUserFromGroupDropsCachedRoleAtOnce() {
    warmCaches();

    groupUserRepository.deleteById(new GroupUserId(rebel.getId(), chubaka.getId()));

    assertCachesEmpty();
    assertThat(membershipRepository.findUserProjectRoles(chubaka.getId(), falcon.getId()))
        .containsExactly(ProjectRole.VIEWER);
    assertThat(membershipRepository.findMembershipDetails(chubaka.getId(), falcon.getName()).orElseThrow()
        .getProjectRole()).isEqualTo(ProjectRole.VIEWER);
  }

  @Test
  void loweringGroupProjectRoleDropsCachedRoleAtOnce() {
    warmCaches();

    var groupProject = groupProjectRepository.findById(new GroupProjectId(rebel.getId(), falcon.getId()))
        .orElseThrow();
    groupProject.setProjectRole(ProjectRole.VIEWER);
    groupProjectRepository.saveAndFlush(groupProject);

    assertCachesEmpty();
    assertThat(membershipRepository.findUserProjectRoles(chubaka.getId(), falcon.getId()))
        .containsExactly(ProjectRole.VIEWER);
    assertThat(membershipRepository.findMembershipDetails(chubaka.getId(), falcon.getId()).orElseThrow()
        .getProjectRole()).isEqualTo(ProjectRole.VIEWER);
  }

  @Test
  void removingGroupFromProjectDropsCachedRoleAtOnce() {
    warmCaches();

    groupProjectRepository.deleteById(new GroupProjectId(rebel.getId(), falcon.getId()));

    assertCachesEmpty();
    assertThat(membershipRepository.findUserProjectRoles(chubaka.getId(), falcon.getId()))
        .containsExactly(ProjectRole.VIEWER);
  }

  @Test
  void deletingGroupDropsCachedRoleAtOnce() {
    warmCaches();

    groupRepository.deleteById(rebel.getId());
    groupRepository.flush();

    assertCachesEmpty();
    assertThat(membershipRepository.findUserProjectRoles(chubaka.getId(), falcon.getId()))
        .containsExactly(ProjectRole.VIEWER);
  }

  @Test
  void readsDoNotEvict() {
    warmCaches();

    groupUserRepository.findAllByGroupId(ewoks.getId());
    groupProjectRepository.findAllByGroupId(ewoks.getId());
    groupRepository.findBySlug("ewoks-group");

    assertThat(rolesCache().get(chubaka.getId() + "_" + falcon.getId())).isNotNull();
  }
}
