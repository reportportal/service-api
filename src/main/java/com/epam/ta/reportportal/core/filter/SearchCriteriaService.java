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

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.filter.predefined.PredefinedFilterType;
import com.epam.ta.reportportal.ws.model.SearchCriteria;
import com.epam.ta.reportportal.ws.model.SearchCriteriaRQ;
import java.util.Set;

/**
 * Service for converting SearchCriteria to Filter.
 *
 * @author Ryhor_Kukharenka
 */
public interface SearchCriteriaService {

  Queryable createFilterBySearchCriteria(SearchCriteriaRQ searchCriteriaRQ,
      Class<?> target, PredefinedFilterType predefinedFilterType);

}
