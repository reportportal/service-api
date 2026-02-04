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

import com.epam.reportportal.base.infrastructure.persistence.dao.GroupProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.GroupRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.Group;
import com.epam.reportportal.base.infrastructure.persistence.entity.group.GroupProject;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

/**
 * Test class for {@link GroupProjectRepository}.
 *
 * @author <a href="mailto:Reingold_Shekhtel@epam.com">Reingold Shekhtel</a>
 */
@Sql("/db/fill/group/group-fill.sql")
class GroupProjectRepositoryTest extends BaseMvcTest {

  @Autowired
  private GroupProjectRepository groupProjectRepository;
  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private EntityManager entityManager;

  private Statistics statistics;

  private Group rebel;

  @BeforeEach
  void setUp() {
    rebel = groupRepository.findBySlug("rebel-group")
        .orElseThrow(() -> new RuntimeException("Group not found"));
    Session session = entityManager.unwrap(Session.class);
    statistics = session.getSessionFactory().getStatistics();
    statistics.setStatisticsEnabled(true);
    statistics.clear();
  }

  @Test
  void testFindAllGroupProjects() {
    List<GroupProject> groupProjects =
        groupProjectRepository.findAllByGroupId(rebel.getId());
    assertEquals(1, groupProjects.size());
  }

  @Test
  void testFindProjectsPageByGroupId() {
    var pageable = PageRequest.of(0, 10);
    Page<GroupProject> groupProjects =
        groupProjectRepository.findAllByGroupId(rebel.getId(),
            pageable);
    assertNotNull(groupProjects);
    assertEquals(1, groupProjects.getTotalElements());
  }

  @Test
  void testFindAllByProjectName() {
    var groupProjectsPage = groupProjectRepository.findAllByProjectName(
        "millennium_falcon",
        null
    );
    groupProjectsPage.forEach(g ->
        {
          Hibernate.initialize(g.getGroup().getSlug());
          Hibernate.initialize(g.getGroup().getUsers().size());
        }
    );

    assertEquals(1, statistics.getQueryExecutionCount());
    assertEquals(1, statistics.getPrepareStatementCount());
    assertEquals(3, groupProjectsPage.getContent().size());
  }

  @Test
  void testFindByGroupIdAndProjectName() {
    var group = groupProjectRepository.findByGroupIdAndProjectName(
        rebel.getId(),
        "millennium_falcon"
    ).orElseThrow(
        () -> new RuntimeException("Group project not found")
    );
    Hibernate.initialize(group.getGroup().getSlug());
    Hibernate.initialize(group.getGroup().getUsers().size());

    assertEquals(1, statistics.getQueryExecutionCount());
    assertEquals(1, statistics.getPrepareStatementCount());
  }
}
