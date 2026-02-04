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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.UserFilterRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author Ivan Nikitsenka
 */
@Sql("/db/fill/shareable/shareable-fill.sql")
class UserFilterRepositoryTest extends BaseMvcTest {

  @Autowired
  private UserFilterRepository userFilterRepository;

  @Test
  public void shouldFindByIdAndProjectIdWhenExists() {
    Optional<UserFilter> userFilter = userFilterRepository.findByIdAndProjectId(1L, 1L);

    assertTrue(userFilter.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenIdNotExists() {
    Optional<UserFilter> userFilter = userFilterRepository.findByIdAndProjectId(55L, 1L);

    assertFalse(userFilter.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenProjectIdNotExists() {
    Optional<UserFilter> userFilter = userFilterRepository.findByIdAndProjectId(5L, 11L);

    assertFalse(userFilter.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenIdAndProjectIdNotExist() {
    Optional<UserFilter> userFilter = userFilterRepository.findByIdAndProjectId(55L, 11L);

    assertFalse(userFilter.isPresent());
  }

  @Test
  public void shouldFindByIdsAndProjectIdWhenExists() {
    List<UserFilter> userFilters = userFilterRepository.findAllByIdInAndProjectId(
        Lists.newArrayList(1L, 2L), 1L);

    assertNotNull(userFilters);
    assertEquals(2L, userFilters.size());
  }

  @Test
  public void shouldNotFindByIdsAndProjectIdWhenProjectIdNotExists() {
    List<UserFilter> userFilters = userFilterRepository.findAllByIdInAndProjectId(
        Lists.newArrayList(1L, 2L), 2L);

    assertNotNull(userFilters);
    assertTrue(userFilters.isEmpty());
  }

  @Test
  void existsByNameAndOwnerAndProjectIdTest() {
    assertTrue(
        userFilterRepository.existsByNameAndOwnerAndProjectId("Admin Filter", "superadmin", 1L));
    assertTrue(
        userFilterRepository.existsByNameAndOwnerAndProjectId("Default Shared Filter", "default",
            2L));
    assertFalse(userFilterRepository.existsByNameAndOwnerAndProjectId("DEMO_FILTER", "yahoo", 1L));
    assertFalse(
        userFilterRepository.existsByNameAndOwnerAndProjectId("Admin Filter", "superadmin", 2L));
  }

  @Test
  void existsByNameAndProjectIdTest() {
    assertTrue(
        userFilterRepository.existsByNameAndProjectId("Admin Filter", 1L));
    assertFalse(userFilterRepository.existsByNameAndProjectId("DEMO_FILTER", 1L));
  }


  @Test
  void findAllByProjectId() {
    final Long projectId = 1L;
    final List<UserFilter> filters = userFilterRepository.findAllByProjectId(projectId);
    assertNotNull(filters, "Filters not found");
    assertTrue(!filters.isEmpty(), "Filters should not be empty");
    filters.forEach(it -> assertEquals(projectId, it.getProject().getId()));
  }

  private Filter buildDefaultFilter() {
    return Filter.builder()
        .withTarget(UserFilter.class)
        .withCondition(
            new FilterCondition(Condition.EQUALS, false, "Default Filter", CRITERIA_NAME))
        .build();
  }

  @Test
  void shouldFindByFilterAndPage() {
    Filter filter = buildDefaultFilter();
    Pageable pageable = PageRequest.of(1, 50, Sort.sort(Dashboard.class));
    Page<UserFilter> page = userFilterRepository.findByFilter(filter, pageable);
    assertEquals(1, page.getTotalElements());
  }

  @Test
  void shouldFindByFilter() {
    Filter filter = buildDefaultFilter();
    List<UserFilter> byFilter = userFilterRepository.findByFilter(filter);
    assertEquals(1, byFilter.size());
  }
}
