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

package com.epam.reportportal.infrastructure.persistence.commons.querygen;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant;

public class ProjectFilter extends Filter {

  private ProjectFilter(Long id, Queryable filter, Long projectId) {
    super(id, filter.getTarget(), filter.getFilterConditions());
    getFilterConditions().add(new FilterCondition(Condition.EQUALS,
        false,
        String.valueOf(projectId),
        GeneralCriteriaConstant.CRITERIA_PROJECT_ID
    ));

  }

  private ProjectFilter(Queryable filter, Long projectId) {
    super(filter.getTarget(), filter.getFilterConditions());
    getFilterConditions().add(new FilterCondition(Condition.EQUALS,
        false,
        String.valueOf(projectId),
        GeneralCriteriaConstant.CRITERIA_PROJECT_ID
    ));

  }

  public static ProjectFilter of(Queryable filter, Long projectId) {
    return new ProjectFilter(filter, projectId);
  }

  public static ProjectFilter of(Long id, Queryable filter, Long projectId) {
    return new ProjectFilter(id, filter, projectId);
  }

}
