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

package com.epam.reportportal.base.infrastructure.persistence.dao.widget.healthcheck.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.HealthCheckTableGetParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck.LevelEntry;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Checks that the materialized view name is rendered as a quoted identifier, never as plain SQL.
 */
class HealthCheckTableQueryProviderTest {

  private static final String MALICIOUS_VIEW = "widget_1_2 x; DROP TABLE launch; --";

  private final DSLContext dsl = DSL.using(SQLDialect.POSTGRES);

  @Test
  void statisticsQueryQuotesViewName() {
    StatisticsQueryProvider provider = new StatisticsQueryProvider();
    ReflectionTestUtils.setField(provider, "dsl", dsl);

    String sql = dsl.render(provider.apply(params(MALICIOUS_VIEW)));

    assertQuoted(sql);
  }

  @Test
  void customColumnQueryQuotesViewName() {
    CustomColumnQueryProvider provider = new CustomColumnQueryProvider(dsl);

    String sql = dsl.render(provider.apply(params(MALICIOUS_VIEW)));

    assertQuoted(sql);
  }

  @Test
  void qualifiedColumnsMatchQuotedViewTable() {
    StatisticsQueryProvider provider = new StatisticsQueryProvider();
    ReflectionTestUtils.setField(provider, "dsl", dsl);

    String sql = dsl.renderInlined(provider.apply(params("widget_1_2")));

    assertTrue(sql.contains("from \"widget_1_2\""), sql);
    assertTrue(sql.contains("\"widget_1_2\".\"item_id\""), sql);
  }

  private static void assertQuoted(String sql) {
    assertTrue(sql.contains("\"widget_1_2 x; DROP TABLE launch; --\""), sql);
    assertFalse(sql.contains("from widget_1_2 x"), sql);
  }

  private static HealthCheckTableGetParams params(String viewName) {
    return HealthCheckTableGetParams.of(viewName, "build", Sort.unsorted(), true,
        List.of(LevelEntry.of("k1", "v1")), false);
  }
}
