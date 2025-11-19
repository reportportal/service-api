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

package com.epam.reportportal.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.group.Group;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test class for {@link GroupRepository}.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Sql("/db/fill/group/group-fill.sql")
public class GroupRepositoryTest extends BaseMvcTest {

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private EntityManager entityManager;

  private Statistics statistics;

  private Group rebelGroup;

  @BeforeEach
  void setUp() {
    rebelGroup = groupRepository.findBySlug("rebel-group")
        .orElseThrow(() -> new RuntimeException("Group not found"));
    Session session = entityManager.unwrap(Session.class);
    statistics = session.getSessionFactory().getStatistics();
    statistics.setStatisticsEnabled(true);
    statistics.clear();
  }

  @Test
  void testGroupCreation() {
    groupRepository.save(new Group("Test group", "test-group", 1L, 1L));
    var group = groupRepository.findBySlug("test-group")
        .orElseThrow(() -> new RuntimeException("Group not found")
        );

    group.setSlug("new-test-group");
    groupRepository.save(group);
    assertTrue(groupRepository.findBySlug("new-test-group").isPresent());
  }

  @Test
  void testFindByUuid() {
    assertTrue(groupRepository.findByUuid(rebelGroup.getUuid()).isPresent());
  }

  @Test
  void testFindAllWithUsersAndProjects() {
    var groups = groupRepository.findAllWithUsersAndProjects(null);
    assertEquals(5, groups.getContent().size());

    groups.forEach(group -> {
      Hibernate.initialize(group.getUsers().size());
      Hibernate.initialize(group.getProjects().size());
    });

    assertEquals(1, statistics.getQueryExecutionCount());
    assertEquals(1, statistics.getPrepareStatementCount());
  }

  @Test
  void testFindAllWithUsersAndProjectsFilteredByOrgId() {
    var groups = groupRepository.findAllWithUsersAndProjects(1L, null);
    assertEquals(5, groups.getContent().size());

    groups.forEach(group -> {
      Hibernate.initialize(group.getUsers().size());
      Hibernate.initialize(group.getProjects().size());
    });

    assertEquals(1, statistics.getQueryExecutionCount());
    assertEquals(1, statistics.getPrepareStatementCount());
  }

  @Test
  void testFindByIdWithUsersAndProjects() {
    var group = groupRepository.findByIdWithUsersAndProjects(rebelGroup.getId())
        .orElseThrow(() -> new RuntimeException("Group not found")
        );

    Hibernate.initialize(group.getUsers().size());
    Hibernate.initialize(group.getProjects().size());

    assertEquals(1, statistics.getQueryExecutionCount());
    assertEquals(1, statistics.getPrepareStatementCount());
  }
}
