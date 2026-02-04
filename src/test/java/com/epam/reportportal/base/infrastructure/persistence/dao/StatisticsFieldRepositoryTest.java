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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.epam.reportportal.base.infrastructure.persistence.dao.StatisticsFieldRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.statistics.StatisticsField;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/item/items-fill.sql")
class StatisticsFieldRepositoryTest extends BaseMvcTest {

  @Autowired
  private StatisticsFieldRepository repository;

  @Test
  void deleteByName() {
    final String fieldName = "statistics$executions$failed";

    repository.deleteByName(fieldName);
    final List<StatisticsField> statisticsField = repository.findAll();

    assertEquals(13, statisticsField.size());
    statisticsField.forEach(it -> assertNotEquals(fieldName, it.getName()));
  }
}
