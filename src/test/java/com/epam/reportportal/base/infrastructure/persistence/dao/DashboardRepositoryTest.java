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

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.ws.BaseMvcTest;
import jakarta.persistence.EntityManager;
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
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/shareable/shareable-fill.sql")
class DashboardRepositoryTest extends BaseMvcTest {

  @Autowired
  private DashboardRepository dashboardRepository;

  @Autowired
  private UserFilterRepository filterRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  public void shouldFindByIdAndProjectIdWhenExists() {
    Optional<Dashboard> dashboard = dashboardRepository.findByIdAndProjectId(13L, 1L);

    assertTrue(dashboard.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenIdNotExists() {
    Optional<Dashboard> dashboard = dashboardRepository.findByIdAndProjectId(55L, 1L);

    assertFalse(dashboard.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenProjectIdNotExists() {
    Optional<Dashboard> dashboard = dashboardRepository.findByIdAndProjectId(5L, 11L);

    assertFalse(dashboard.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenIdAndProjectIdNotExist() {
    Optional<Dashboard> dashboard = dashboardRepository.findByIdAndProjectId(55L, 11L);

    assertFalse(dashboard.isPresent());
  }

  @Test
  void findAllByProjectId() {
    final long superadminProjectId = 1L;

    final List<Dashboard> dashboards = dashboardRepository.findAllByProjectId(superadminProjectId);

    assertNotNull(dashboards, "Dashboards should not be null");
    assertEquals(4, dashboards.size(), "Unexpected dashboards size");
    dashboards.forEach(it -> assertEquals(superadminProjectId, (long) it.getProject().getId(),
        "Dashboard has incorrect project id"));
  }

  @Test
  void shouldFindBySpecifiedNameAndProjectId() {
    assertTrue(dashboardRepository.existsByNameAndProjectId("test admin dashboard", 1L));
  }

  private Filter buildDefaultFilter() {
    return Filter.builder()
        .withTarget(Dashboard.class)
        .withCondition(
            new FilterCondition(Condition.EQUALS, false, "test admin dashboard", CRITERIA_NAME))
        .build();
  }

  @Test
  void shouldFindByFilterAndSortByLocked() {
    Filter filter = buildDefaultFilter();
    Pageable pageable = PageRequest.of(1, 50, Sort.by("locked"));
    Page<Dashboard> page = dashboardRepository.findByFilter(filter, pageable);
    assertEquals(1, page.getTotalElements());
  }

  @Test
  void shouldFindByFilter() {
    Filter filter = buildDefaultFilter();
    List<Dashboard> byFilter = dashboardRepository.findByFilter(filter);
    assertEquals(1, byFilter.size());
  }

  @Test
  void toggleDashboardLock() {
    dashboardRepository.lockDashboard(13L);
    Dashboard dashboard = dashboardRepository.findById(13L).get();
    assertTrue(dashboard.getLocked());
  }

  @Test
  void lockDashboardShouldNotLockFilters() {
    dashboardRepository.lockDashboard(13L);
    entityManager.flush();
    entityManager.clear();

    // Dashboard should be locked
    Dashboard dashboard = dashboardRepository.findById(13L).get();
    assertTrue(dashboard.getLocked());

    // Filters should NOT be locked
    assertFalse(filterRepository.findById(2L).get().getLocked());
  }

}
