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

package com.epam.ta.reportportal.core.filter;

import com.epam.reportportal.api.model.SearchCriteriaRQ;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType;

/**
 * Service for converting SearchCriteria to Filter.
 *
 * @author Ryhor_Kukharenka
 */
public interface SearchCriteriaService {

  /**
   * Creates a filter based on the provided search criteria and target class.
   *
   * @param searchCriteriaRq The search criteria request object containing filter conditions.
   * @param target           The target class for which the filter is being created.
   * @return A Queryable object representing the filter.
   */
  Queryable createFilterBySearchCriteria(SearchCriteriaRQ searchCriteriaRq, Class<?> target);

  /**
   * Import for the SearchCriteriaRQ model, which represents the request object containing search criteria for
   * filtering.
   */
  Queryable createFilterBySearchCriteria(SearchCriteriaRQ searchCriteriaRq, Class<?> target,
      PredefinedFilterType predefinedFilterType);

}
