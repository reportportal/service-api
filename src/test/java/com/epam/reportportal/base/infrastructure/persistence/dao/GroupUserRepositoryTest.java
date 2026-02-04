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

import com.epam.reportportal.base.infrastructure.persistence.dao.GroupRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.GroupUserRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test class for {@link GroupUserRepository}.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Sql("/db/fill/group/group-fill.sql")
class GroupUserRepositoryTest extends BaseMvcTest {

  @Autowired
  private GroupUserRepository groupUserRepository;
  @Autowired
  private GroupRepository groupRepository;

  private Group rebel;

  @BeforeEach
  void setUp() {
    rebel = groupRepository.findBySlug("rebel-group")
        .orElseThrow(() -> new RuntimeException("Group not found"));
  }

  @Test
  void shouldFindAllUsersByGroupId() {
    var groupUsers = groupUserRepository.findAllByGroupId(rebel.getId());
    assertNotNull(groupUsers);
    assertEquals(2, groupUsers.size());
  }

  @Test
  void shouldFindUsersPageByGroupId() {
    var pageable = PageRequest.of(0, 10);
    var groupUsers = groupUserRepository.findAllByGroupId(rebel.getId(), pageable);
    assertNotNull(groupUsers);
    assertEquals(2, groupUsers.getTotalElements());
  }
}
