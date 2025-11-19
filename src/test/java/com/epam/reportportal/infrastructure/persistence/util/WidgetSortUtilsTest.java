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

package com.epam.reportportal.infrastructure.persistence.util;

import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.SF_NAME;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_COUNTER;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.STATISTICS_TABLE;
import static com.epam.reportportal.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static com.epam.reportportal.infrastructure.persistence.jooq.tables.JLaunch.LAUNCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterTarget;
import java.util.List;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
class WidgetSortUtilsTest {

  private Sort sort;
  private FilterTarget filterTarget;

  @BeforeEach
  void setUp() {

    sort = Sort.by("startTime", "name", "statistics$defects$to_investigate$ti001",
        "statistics$defects$system_issue$si001");
    filterTarget = FilterTarget.LAUNCH_TARGET;
  }

  @Test
  void widgetSortTest() {
    List<SortField<Object>> sortFields = WidgetSortUtils.TO_SORT_FIELDS.apply(sort, filterTarget);

    assertEquals(LAUNCH.START_TIME.getQualifiedName().toString(), sortFields.get(0).getName());
    assertEquals(LAUNCH.NAME.getQualifiedName().toString(), sortFields.get(1).getName());

    assertEquals(DSL.coalesce(DSL.max(fieldName(STATISTICS_TABLE, STATISTICS_COUNTER))
            .filterWhere(fieldName(STATISTICS_TABLE, SF_NAME).cast(String.class)
                .eq("statistics$defects$to_investigate$ti001")), 0)
        .toString(), sortFields.get(2).getName());

    assertEquals(DSL.coalesce(DSL.max(fieldName(STATISTICS_TABLE, STATISTICS_COUNTER))
            .filterWhere(fieldName(STATISTICS_TABLE, SF_NAME).cast(String.class)
                .eq("statistics$defects$system_issue$si001")), 0)
        .toString(), sortFields.get(3).getName());
  }

}
