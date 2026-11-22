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

package com.epam.reportportal.base.core.item.impl.provider;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserFilterRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.ObjectType;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.Statistics;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class FilterDataProviderImplTest {

  private static final long PROJECT_ID = 1L;
  private static final long FILTER_ID = 10L;

  @Mock
  private UserFilterRepository filterRepository;

  @Mock
  private TestItemRepository testItemRepository;

  @InjectMocks
  private FilterDataProviderImpl provider;

  private final MembershipDetails membership = MembershipDetails.builder()
      .withProjectId(PROJECT_ID)
      .withProjectName("project")
      .build();

  @Test
  void statisticsAreScopedToLaunchesOfProjectFilter() {
    when(filterRepository.findByIdAndProjectId(FILTER_ID, PROJECT_ID)).thenReturn(
        Optional.of(launchFilter()));
    Set<Statistics> expected = Collections.singleton(new Statistics());
    when(testItemRepository.accumulateStatisticsByFilter(anyBoolean(), any(Queryable.class),
        any(Queryable.class), any(Pageable.class))).thenReturn(expected);

    Queryable itemFilter = itemFilterForLaunch(999L);
    Set<Statistics> result = provider.accumulateStatistics(itemFilter, membership, null,
        Map.of("filterId", String.valueOf(FILTER_ID), "launchesLimit", "600", "isLatest", "true"));

    assertSame(expected, result);
    ArgumentCaptor<Queryable> launchFilterCaptor = ArgumentCaptor.forClass(Queryable.class);
    ArgumentCaptor<Pageable> launchPageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(testItemRepository).accumulateStatisticsByFilter(eq(true), launchFilterCaptor.capture(),
        eq(itemFilter), launchPageableCaptor.capture());
    verify(testItemRepository, never()).accumulateStatisticsByFilter(any(Queryable.class));

    assertTrue(launchFilterCaptor.getValue().getFilterConditions().stream()
            .flatMap(c -> c.getAllConditions().stream())
            .anyMatch(c -> CRITERIA_PROJECT_ID.equals(c.getSearchCriteria())
                && String.valueOf(PROJECT_ID).equals(c.getValue())
                && c.getCondition() == Condition.EQUALS && !c.isNegative()),
        "launch filter must be restricted to the caller's project");
    assertEquals(600, launchPageableCaptor.getValue().getPageSize());
  }

  @Test
  void statisticsUseSameLaunchSelectionAsItemListing() {
    when(filterRepository.findByIdAndProjectId(FILTER_ID, PROJECT_ID)).thenReturn(
        Optional.of(launchFilter()));
    Map<String, String> params = Map.of("filterId", String.valueOf(FILTER_ID), "launchesLimit",
        "10");
    Queryable itemFilter = itemFilterForLaunch(1L);

    provider.accumulateStatistics(itemFilter, membership, null, params);
    provider.getTestItems(itemFilter, Pageable.ofSize(20), membership, null, params);

    ArgumentCaptor<Queryable> statsLaunchFilter = ArgumentCaptor.forClass(Queryable.class);
    ArgumentCaptor<Pageable> statsLaunchPageable = ArgumentCaptor.forClass(Pageable.class);
    verify(testItemRepository).accumulateStatisticsByFilter(eq(false), statsLaunchFilter.capture(),
        eq(itemFilter), statsLaunchPageable.capture());
    ArgumentCaptor<Queryable> listLaunchFilter = ArgumentCaptor.forClass(Queryable.class);
    ArgumentCaptor<Pageable> listLaunchPageable = ArgumentCaptor.forClass(Pageable.class);
    verify(testItemRepository).findByFilter(eq(false), listLaunchFilter.capture(), eq(itemFilter),
        listLaunchPageable.capture(), any(Pageable.class));

    assertEquals(listLaunchFilter.getValue().getFilterConditions(),
        statsLaunchFilter.getValue().getFilterConditions());
    assertEquals(listLaunchPageable.getValue(), statsLaunchPageable.getValue());
  }

  @Test
  void foreignFilterIdIsRejected() {
    when(filterRepository.findByIdAndProjectId(FILTER_ID, PROJECT_ID)).thenReturn(Optional.empty());

    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> provider.accumulateStatistics(itemFilterForLaunch(1L), membership, null,
            Map.of("filterId", String.valueOf(FILTER_ID), "launchesLimit", "10")));
    ReportPortalException listingEx = assertThrows(ReportPortalException.class,
        () -> provider.getTestItems(itemFilterForLaunch(1L), Pageable.ofSize(20), membership, null,
            Map.of("filterId", String.valueOf(FILTER_ID), "launchesLimit", "10")));

    assertEquals(ErrorType.USER_FILTER_NOT_FOUND_IN_PROJECT, ex.getErrorType());
    assertEquals(listingEx.getMessage(), ex.getMessage());
    verifyNoInteractions(testItemRepository);
  }

  @Test
  void missingLaunchesLimitIsRejectedLikeItemListing() {
    ReportPortalException ex = assertThrows(ReportPortalException.class,
        () -> provider.accumulateStatistics(itemFilterForLaunch(1L), membership, null,
            Map.of("filterId", String.valueOf(FILTER_ID))));

    assertEquals(ErrorType.BAD_REQUEST_ERROR, ex.getErrorType());
    assertFalse(ex.getMessage().isEmpty());
    verifyNoInteractions(testItemRepository);
  }

  private static UserFilter launchFilter() {
    UserFilter filter = new UserFilter();
    filter.setId(FILTER_ID);
    filter.setTargetClass(ObjectType.Launch);
    return filter;
  }

  private static Queryable itemFilterForLaunch(long launchId) {
    return Filter.builder()
        .withTarget(TestItem.class)
        .withCondition(FilterCondition.builder()
            .eq(CRITERIA_LAUNCH_ID, String.valueOf(launchId))
            .build())
        .build();
  }
}
