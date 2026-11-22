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

package com.epam.reportportal.base.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.dao.GroupMembershipRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProjectExtractorTest {

  private final ProjectUserRepository projectUserRepository = mock(ProjectUserRepository.class);
  private final GroupMembershipRepository groupMembershipRepository = mock(GroupMembershipRepository.class);
  private final ProjectExtractor extractor = new ProjectExtractor(projectUserRepository, groupMembershipRepository);

  @Test
  void mergingDirectRoleDoesNotMutateTheListHeldByTheGroupRolesCache() {
    // the repository returns the very list instance stored in groupUserProjectRolesCache
    List<ProjectRole> cachedGroupRoles = new ArrayList<>(List.of(ProjectRole.VIEWER));
    when(groupMembershipRepository.findUserProjectRoles(1L, 10L)).thenReturn(cachedGroupRoles);
    when(projectUserRepository.findDetailsByUserIdAndProjectKey(1L, "p")).thenAnswer(i -> Optional.of(
        MembershipDetails.builder().withProjectId(10L).withProjectRole(ProjectRole.EDITOR).build()));

    var details = extractor.findMembershipDetails(1L, "p").orElseThrow();

    assertThat(details.getProjectRole()).isEqualTo(ProjectRole.EDITOR);
    assertThat(cachedGroupRoles).containsExactly(ProjectRole.VIEWER);
  }
}
