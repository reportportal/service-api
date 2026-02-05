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

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetType;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * Uses script from
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Sql("/db/fill/shareable/shareable-fill.sql")
public class WidgetRepositoryTest extends BaseMvcTest {

  @Autowired
  private WidgetRepository repository;

  @Test
  void findAllByProjectId() {
    final long superadminProjectId = 1L;

    final List<Widget> widgets = repository.findAllByProjectId(superadminProjectId);

    assertNotNull(widgets, "Widgets not found");
    assertEquals(5, widgets.size(), "Unexpected widgets size");
    widgets.forEach(
        it -> assertEquals(superadminProjectId, (long) it.getProject().getId(), "Widget has incorrect project id"));
  }

  @Test
  public void shouldFindByIdAndProjectIdWhenExists() {
    Optional<Widget> widget = repository.findByIdAndProjectId(5L, 1L);

    assertTrue(widget.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenIdNotExists() {
    Optional<Widget> widget = repository.findByIdAndProjectId(55L, 1L);

    assertFalse(widget.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenProjectIdNotExists() {
    Optional<Widget> widget = repository.findByIdAndProjectId(5L, 11L);

    assertFalse(widget.isPresent());
  }

  @Test
  public void shouldNotFindByIdAndProjectIdWhenIdAndProjectIdNotExist() {
    Optional<Widget> widget = repository.findByIdAndProjectId(55L, 11L);

    assertFalse(widget.isPresent());
  }

  @Test
  void existsByNameAndOwnerAndProjectId() {
    assertTrue(repository.existsByNameAndOwnerAndProjectId("INVESTIGATED PERCENTAGE OF LAUNCHES", "superadmin", 1L));
    assertFalse(repository.existsByNameAndOwnerAndProjectId("not exist name", "default", 2L));
  }

  @Test
  void deleteRelationByFilterIdAndNotOwnerTest() {

    int removedCount = repository.deleteRelationByFilterIdAndNotOwner(2L, "superadmin");

    Assertions.assertEquals(1, removedCount);
  }

  @Test
  void findAllByProjectIdAndWidgetTypeInTest() {
    List<Widget> widgets = repository.findAllByProjectIdAndWidgetTypeIn(1L,
        Lists.newArrayList(WidgetType.LAUNCH_STATISTICS, WidgetType.CASES_TREND)
            .stream()
            .map(WidgetType::getType)
            .collect(Collectors.toList())
    );

    assertFalse(widgets.isEmpty());
    assertEquals(2, widgets.size());

    List<Widget> moreWidgets = repository.findAllByProjectIdAndWidgetTypeIn(1L,
        Collections.singletonList(WidgetType.CASES_TREND.getType())
    );

    assertFalse(moreWidgets.isEmpty());
    assertEquals(1, moreWidgets.size());
  }

  @Test
  void findAllByOwnerAndWidgetTypeInTest() {
    List<Widget> widgets = repository.findAllByOwnerAndWidgetTypeIn("superadmin",
        Lists.newArrayList(WidgetType.LAUNCH_STATISTICS, WidgetType.ACTIVITY)
            .stream()
            .map(WidgetType::getType)
            .collect(Collectors.toList())
    );

    assertFalse(widgets.isEmpty());
    assertEquals(2, widgets.size());

    List<Widget> moreWidgets = repository.findAllByOwnerAndWidgetTypeIn("superadmin",
        Collections.singletonList(WidgetType.LAUNCH_STATISTICS.getType())
    );

    assertFalse(moreWidgets.isEmpty());
    assertEquals(1, moreWidgets.size());
  }

  @Test
  void findAllByWidgetTypeInAndContentFieldsContainsTest() {
    List<Widget> widgets = repository.findAllByProjectIdAndWidgetTypeInAndContentFieldsContains(1L,
        Lists.newArrayList(WidgetType.LAUNCH_STATISTICS, WidgetType.LAUNCHES_TABLE)
            .stream()
            .map(WidgetType::getType)
            .collect(Collectors.toList()),
        "statistics$product_bug$pb001"
    );

    assertFalse(widgets.isEmpty());
    assertEquals(1, widgets.size());
  }

  @Test
  void findAllByProjectIdWidgetTypeInAndContentFieldContainingTest() {
    List<Widget> widgets = repository.findAllByProjectIdAndWidgetTypeInAndContentFieldContaining(1L,
        Lists.newArrayList(WidgetType.LAUNCH_STATISTICS, WidgetType.LAUNCHES_TABLE)
            .stream()
            .map(WidgetType::getType)
            .collect(Collectors.toList()),
        "statistics$product_bug"
    );

    assertFalse(widgets.isEmpty());
    assertEquals(1, widgets.size());
  }

  private Filter buildDefaultFilter() {
    return Filter.builder()
        .withTarget(Widget.class)
        .withCondition(new FilterCondition(Condition.LOWER_THAN, false, "100", CRITERIA_ID))
        .build();
  }
}
