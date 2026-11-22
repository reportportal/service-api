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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.jooq.DSLContext;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class ProjectInfoCteQueryBuilderTest {

  private final DSLContext dsl = mock(DSLContext.class);
  private final ProjectInfoCteQueryBuilder builder = new ProjectInfoCteQueryBuilder(dsl);

  private static Filter filter(FilterCondition condition) {
    return Filter.builder().withTarget(ProjectInfo.class).withCondition(condition).build();
  }

  private static FilterCondition between(String value) {
    return FilterCondition.builder()
        .withCondition(Condition.BETWEEN)
        .withSearchCriteria("creationDate")
        .withValue(value)
        .build();
  }

  @Test
  void malformedBetweenIsRejected() {
    for (String value : new String[] {"garbage", "1;2", "a;b;+03:00", "1,2,3", "1;2;nonsense", "1,2;x"}) {
      ReportPortalException exception = assertThrows(ReportPortalException.class,
          () -> builder.findProjectInfo(filter(between(value))), value);
      assertEquals(ErrorType.INCORRECT_FILTER_PARAMETERS, exception.getErrorType(), value);
    }
  }

  @Test
  void uncastableValueIsRejected() {
    for (String[] c : new String[][] {{"id", "abc"}, {"creationDate", "notadate"}}) {
      FilterCondition condition = FilterCondition.builder()
          .withCondition(Condition.EQUALS)
          .withSearchCriteria(c[0])
          .withValue(c[1])
          .build();
      ReportPortalException exception = assertThrows(ReportPortalException.class,
          () -> builder.findProjectInfo(filter(condition)), c[1]);
      assertEquals(ErrorType.INCORRECT_FILTER_PARAMETERS, exception.getErrorType(), c[1]);
    }
  }

  @Test
  void unknownSortPropertyIsRejected() {
    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> builder.findPagedProjectInfo(filter(between("1,2")),
            PageRequest.of(0, 10, Sort.by("statisticsFoo"))));
    assertEquals(ErrorType.INCORRECT_SORTING_PARAMETERS, exception.getErrorType());
  }

  @Test
  void knownSortPropertiesAndValidBetweenAreAccepted() {
    when(dsl.fetch(any(ResultQuery.class))).thenReturn(
        DSL.using(SQLDialect.POSTGRES).newResult(DSL.field("x")));
    for (String property : new String[] {"creationDate", "usersQuantity", "launchesQuantity",
        "lastRun", "name", "key", "organization"}) {
      builder.findPagedProjectInfo(filter(between("1000,2000")),
          PageRequest.of(0, 10, Sort.by(property)));
    }
    builder.findProjectInfo(filter(between("0;1440;+03:00")));
  }
}
