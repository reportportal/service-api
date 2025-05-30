/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.core.filter.impl;

import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.reportportal.api.model.SearchCriteriaSearchCriteriaInner;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.querygen.ConvertibleCondition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.filter.FilterOperation;
import com.epam.ta.reportportal.core.filter.OrganizationsSearchCriteriaService;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * Service implementation for converting SearchCriteria to Filter.
 *
 * @author Siarhei Hrabko
 */
@Service
public class OrganizationsSearchCriteriaServiceImpl implements OrganizationsSearchCriteriaService {

  @Override
  public Filter createFilterBySearchCriteria(
      SearchCriteriaRQ searchCriteriaRQ, Class<?> target) {

    Filter filter = new Filter(target, Lists.newArrayList());

    if (CollectionUtils.isNotEmpty(searchCriteriaRQ.getSearchCriteria())) {
      filter.withConditions(collectConditions(searchCriteriaRQ));
    }

    return filter;
  }

  private List<ConvertibleCondition> collectConditions(SearchCriteriaRQ searchCriteriaRQ) {
    return searchCriteriaRQ.getSearchCriteria()
        .stream()
        .map(this::mapCriteriaToCondition)
        .collect(Collectors.toList());
  }

  private ConvertibleCondition mapCriteriaToCondition(
      SearchCriteriaSearchCriteriaInner searchCriteria) {
    return FilterOperation.fromString(searchCriteria.getOperation().toString())
        .map(operation -> operation.getConditionBuilder()
            .withSearchCriteria(searchCriteria.getFilterKey())
            .withValue(searchCriteria.getValue())
            .build())
        .orElseThrow(() -> new ReportPortalException(
            String.format("Can not convert operation type %s.", searchCriteria.getOperation())));
  }

}
