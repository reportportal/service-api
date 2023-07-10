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

import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.filter.FilterOperation;
import com.epam.ta.reportportal.core.filter.SearchCriteriaService;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilters;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.SearchCriteria;
import com.epam.ta.reportportal.ws.model.SearchCriteriaRQ;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.Operator;
import org.springframework.stereotype.Service;

/**
 * Service implementation for converting SearchCriteria to Filter.
 *
 * @author Ryhor_Kukharenka
 */
@Service
public class SearchCriteriaServiceImpl implements SearchCriteriaService {

  private static final String PREDEFINED_FILTER = "predefinedFilter";

  @Override
  public Queryable createFilterBySearchCriteria(SearchCriteriaRQ searchCriteriaRQ,
      Class<?> target, PredefinedFilterType predefinedFilterType) {

    Filter filter = new Filter(target, Lists.newArrayList());

    if (CollectionUtils.isEmpty(searchCriteriaRQ.getCriteriaList())) {
      return filter;
    }

    filter.withConditions(collectConditions(searchCriteriaRQ));

    Optional<SearchCriteria> predefinedFilter = getPredefinedFilterIfExist(searchCriteriaRQ);

    return predefinedFilter.isPresent()
        ? createCompositeFilter(predefinedFilterType, filter, predefinedFilter.get())
        : filter;
  }

  private List<ConvertibleCondition> collectConditions(SearchCriteriaRQ searchCriteriaRQ) {
    return searchCriteriaRQ.getCriteriaList().stream()
        .filter(criteria -> !PREDEFINED_FILTER.equalsIgnoreCase(criteria.getFilterKey()))
        .map(this::mapCriteriaToCondition)
        .collect(Collectors.toList());
  }

  private ConvertibleCondition mapCriteriaToCondition(SearchCriteria searchCriteria) {
    return FilterOperation.fromString(searchCriteria.getOperation())
        .map(operation -> operation.getConditionBuilder()
            .withSearchCriteria(searchCriteria.getFilterKey())
            .withValue(searchCriteria.getValue())
            .build())
        .orElseThrow(() -> new ReportPortalException(
            String.format("Can not convert operation type %s.", searchCriteria.getOperation())));
  }

  private Optional<SearchCriteria> getPredefinedFilterIfExist(SearchCriteriaRQ searchCriteriaRQ) {
    return searchCriteriaRQ.getCriteriaList().stream()
        .filter(criteria -> PREDEFINED_FILTER.equalsIgnoreCase(criteria.getFilterKey()))
        .findFirst();
  }

  private CompositeFilter createCompositeFilter(PredefinedFilterType predefinedFilterType,
      Filter filter, SearchCriteria predefinedFilter) {
    String[] params = {predefinedFilter.getValue()};
    Queryable activityPredefinedFilter = PredefinedFilters
        .buildFilter(predefinedFilterType, params);
    return new CompositeFilter(Operator.AND, filter, activityPredefinedFilter);
  }

}
