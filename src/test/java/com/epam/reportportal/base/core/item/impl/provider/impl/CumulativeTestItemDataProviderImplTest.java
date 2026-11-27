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

package com.epam.reportportal.base.core.item.impl.provider.impl;

import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_LEVEL_ATTRIBUTE;
import static com.epam.reportportal.base.ws.resolver.FilterCriteriaResolver.DEFAULT_FILTER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.ConvertibleCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.WidgetContentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CumulativeTestItemDataProviderImplTest {

  private static final Long PROJECT_ID = 3L;
  private static final String VIEW = "widget_3_10";
  private static final String LEVEL = "build:1.0";

  @Mock
  private WidgetContentRepository widgetContentRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @InjectMocks
  private CumulativeTestItemDataProviderImpl provider;

  private final MembershipDetails membershipDetails =
      MembershipDetails.builder().withProjectId(PROJECT_ID).build();

  @Test
  void getTestItemsReturnsEmptyPageWhenNoRedirectLaunches() {
    when(widgetContentRepository.getCumulativeLevelRedirectLaunchIds(VIEW, LEVEL))
        .thenReturn(Collections.emptyList());
    Pageable pageable = PageRequest.of(0, 20);

    Page<TestItem> result =
        provider.getTestItems(callerFilter(), pageable, membershipDetails, null, params());

    assertTrue(result.isEmpty());
    assertEquals(0, result.getTotalElements());
    verify(testItemRepository, never()).findByFilter(any(Queryable.class), any(Pageable.class));
  }

  @Test
  void accumulateStatisticsReturnsEmptyWhenNoRedirectLaunches() {
    when(widgetContentRepository.getCumulativeLevelRedirectLaunchIds(VIEW, LEVEL))
        .thenReturn(Collections.emptyList());

    Set<Statistics> result =
        provider.accumulateStatistics(callerFilter(), membershipDetails, null, params());

    assertTrue(result.isEmpty());
    verify(testItemRepository, never()).accumulateStatisticsByFilter(any());
  }

  @Test
  void getTestItemsRestrictsQueryToRedirectLaunchesAndProject() {
    when(widgetContentRepository.getCumulativeLevelRedirectLaunchIds(VIEW, LEVEL))
        .thenReturn(List.of(5L, 6L));
    Pageable pageable = PageRequest.of(0, 20);
    when(testItemRepository.findByFilter(any(Queryable.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(Collections.emptyList()));

    provider.getTestItems(callerFilter(), pageable, membershipDetails, null, params());

    ArgumentCaptor<Queryable> captor = ArgumentCaptor.forClass(Queryable.class);
    verify(testItemRepository).findByFilter(captor.capture(), any(Pageable.class));
    assertRestricted(captor.getValue());
  }

  @Test
  void accumulateStatisticsRestrictsQueryToRedirectLaunchesAndProject() {
    when(widgetContentRepository.getCumulativeLevelRedirectLaunchIds(VIEW, LEVEL))
        .thenReturn(List.of(5L, 6L));

    provider.accumulateStatistics(callerFilter(), membershipDetails, null, params());

    ArgumentCaptor<Queryable> captor = ArgumentCaptor.forClass(Queryable.class);
    verify(testItemRepository).accumulateStatisticsByFilter(captor.capture());
    assertRestricted(captor.getValue());
  }

  private void assertRestricted(Queryable queryable) {
    List<FilterCondition> conditions = queryable.getFilterConditions().stream()
        .map(ConvertibleCondition::getAllConditions)
        .flatMap(List::stream)
        .toList();
    assertTrue(conditions.stream().anyMatch(
        c -> CRITERIA_PROJECT_ID.equals(c.getSearchCriteria()) && Condition.EQUALS.equals(
            c.getCondition()) && String.valueOf(PROJECT_ID).equals(c.getValue())));
    assertTrue(conditions.stream().anyMatch(
        c -> CRITERIA_LAUNCH_ID.equals(c.getSearchCriteria()) && Condition.IN.equals(
            c.getCondition()) && "5,6".equals(c.getValue())));
  }

  private static Queryable callerFilter() {
    return Filter.builder().withTarget(TestItem.class)
        .withCondition(FilterCondition.builder().eq("name", "test").build()).build();
  }

  private static Map<String, String> params() {
    Map<String, String> params = new HashMap<>();
    params.put(VIEW_NAME, VIEW);
    params.put(DEFAULT_FILTER_PREFIX + Condition.HAS_FILTER + CRITERIA_LEVEL_ATTRIBUTE, LEVEL);
    return params;
  }
}
