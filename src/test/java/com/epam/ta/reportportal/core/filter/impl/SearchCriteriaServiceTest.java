/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.filter.impl;

import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_EVENT_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.FilterTarget;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.model.SearchCriteria;
import com.epam.ta.reportportal.model.SearchCriteriaRQ;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchCriteriaServiceTest {

  private Class<?> target;
  private SearchCriteriaRQ searchCriteriaRQ;
  private SearchCriteriaServiceImpl searchCriteriaService;

  @BeforeEach
  void setUp() {
    searchCriteriaService = new SearchCriteriaServiceImpl();
    searchCriteriaRQ = new SearchCriteriaRQ();
    target = Activity.class;
  }

  @Test
  void testCreateFilterBySearchCriteria_noCriteria() {
    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES
    );

    assertTrue(filter instanceof Filter);
    assertTrue(CollectionUtils.isEmpty(filter.getFilterConditions()));
    assertEquals(FilterTarget.ACTIVITY_TARGET, filter.getTarget());
  }

  @Test
  void testCreateFilterBySearchCriteria_criteriaWithoutPredefinedFilter() {
    Set<SearchCriteria> criteriaList =
        Set.of(new SearchCriteria("sampleKey1", "EQ", "sampleValue1"),
            new SearchCriteria("sampleKey2", "IN", "sampleValue2")
        );
    searchCriteriaRQ.setCriteriaList(criteriaList);

    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES
    );

    assertTrue(filter instanceof Filter);
    assertEquals(2, filter.getFilterConditions().size());
  }

  @Test
  void testCreateFilterBySearchCriteria_criteriaWithPredefinedFilter() {
    Set<SearchCriteria> criteriaList =
        Set.of(new SearchCriteria("predefinedFilter", null, "predefinedValue"),
            new SearchCriteria("sampleKey3", "EQ", "sampleValue3")
        );
    searchCriteriaRQ.setCriteriaList(criteriaList);

    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES
    );

    assertTrue(filter instanceof CompositeFilter);
    assertEquals(6, filter.getFilterConditions().size());
  }

  @Test
  void testCreateFilterBySearchCriteria_wrongOperation_throwException() {
    Set<SearchCriteria> criteriaList =
        Set.of(new SearchCriteria("sampleKey1", "WRONG", "sampleValue1"));
    searchCriteriaRQ.setCriteriaList(criteriaList);

    Assertions.assertThrows(ReportPortalException.class,
        () -> searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
            PredefinedFilterType.ACTIVITIES
        )
    );

  }

  @Test
  void testNormalizeValueSpaces() {
    Set<SearchCriteria> criteriaList =
        Set.of(new SearchCriteria("predefinedFilter", "CNT", "sample Value1"));
    searchCriteriaRQ.setCriteriaList(criteriaList);
    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES
    );

    filter.getFilterConditions().forEach(cnd -> {
      FilterCondition condition = (FilterCondition) cnd;
      switch (condition.getSearchCriteria()) {
        case CRITERIA_EVENT_NAME:
        case CRITERIA_OBJECT_TYPE:
          Assertions.assertEquals("sampleValue1", condition.getValue());
          break;
        default:
          Assertions.assertEquals("sample Value1", condition.getValue());
      }
    });
  }
}