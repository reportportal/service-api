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

package com.epam.reportportal.base.core.filter.impl;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_EVENT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.api.model.FilterOperation;
import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteriaInner;
import com.epam.reportportal.base.core.filter.predefined.PredefinedFilterType;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.CompositeFilter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterTarget;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Operator;
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

    assertInstanceOf(Filter.class, filter);
    assertTrue(CollectionUtils.isEmpty(filter.getFilterConditions()));
    assertEquals(FilterTarget.ACTIVITY_TARGET, filter.getTarget());
  }

  @Test
  void testCreateFilterBySearchCriteria_criteriaWithoutPredefinedFilter() {
    List<SearchCriteriaSearchCriteriaInner> criteriaList =
        List.of(new SearchCriteriaSearchCriteriaInner("sampleKey1", null, FilterOperation.EQ, "sampleValue1"),
            new SearchCriteriaSearchCriteriaInner("sampleKey2", null, FilterOperation.IN, "sampleValue2")
        );
    searchCriteriaRQ.setSearchCriteria(criteriaList);

    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES
    );

    assertInstanceOf(Filter.class, filter);
    assertEquals(2, filter.getFilterConditions().size());
  }

  @Test
  void testCreateFilterBySearchCriteria_criteriaWithPredefinedFilter() {
    List<SearchCriteriaSearchCriteriaInner> criteriaList =
        List.of(new SearchCriteriaSearchCriteriaInner("predefinedFilter", null, null, "predefinedValue"),
            new SearchCriteriaSearchCriteriaInner("sampleKey3", null, FilterOperation.EQ, "sampleValue3")
        );
    searchCriteriaRQ.setSearchCriteria(criteriaList);

    Queryable filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES
    );

    assertInstanceOf(CompositeFilter.class, filter);
    assertEquals(6, filter.getFilterConditions().size());
  }


  @Test
  void testCreateFilterBySearchCriteria_criteriaWithOrOperator() {
    List<SearchCriteriaSearchCriteriaInner> criteriaList =
        List.of(
            new SearchCriteriaSearchCriteriaInner("sampleKey1", SearchOperator.OR,
                FilterOperation.EQ, "sampleValue1"),
            new SearchCriteriaSearchCriteriaInner("sampleKey2", SearchOperator.OR,
                FilterOperation.IN, "sampleValue2")
        );
    searchCriteriaRQ.setSearchCriteria(criteriaList);

    var filter = searchCriteriaService.createFilterBySearchCriteria(
        searchCriteriaRQ, target, PredefinedFilterType.ACTIVITIES);

    assertInstanceOf(Filter.class, filter);
    assertEquals(2, filter.getFilterConditions().size());
    filter.getFilterConditions().forEach(cnd -> {
      FilterCondition condition = (FilterCondition) cnd;
      assertEquals(Operator.OR, condition.getOperator());
    });
  }

  @Test
  void testCreateFilterBySearchCriteria_criteriaWithMixedOperators() {
    List<SearchCriteriaSearchCriteriaInner> criteriaList =
        List.of(
            new SearchCriteriaSearchCriteriaInner("sampleKey1", null, FilterOperation.EQ,
                "sampleValue1"),
            new SearchCriteriaSearchCriteriaInner("sampleKey2", SearchOperator.OR,
                FilterOperation.IN, "sampleValue2")
        );
    searchCriteriaRQ.setSearchCriteria(criteriaList);

    var filter = searchCriteriaService.createFilterBySearchCriteria(searchCriteriaRQ, target,
        PredefinedFilterType.ACTIVITIES);

    assertInstanceOf(Filter.class, filter);
    List<ConvertibleCondition> conditions = filter.getFilterConditions();
    assertEquals(2, conditions.size());
    assertEquals(Operator.AND, ((FilterCondition) conditions.get(0)).getOperator());
    assertEquals(Operator.OR, ((FilterCondition) conditions.get(1)).getOperator());
  }

  @Test
  void testNormalizeValueSpaces() {
    List<SearchCriteriaSearchCriteriaInner> criteriaList =
        List.of(new SearchCriteriaSearchCriteriaInner("predefinedFilter", null, FilterOperation.CNT, "sample Value1"));
    searchCriteriaRQ.setSearchCriteria(criteriaList);
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
