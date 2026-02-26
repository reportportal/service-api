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

import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JPatternTemplateTestItem.PATTERN_TEMPLATE_TEST_ITEM;

import com.epam.reportportal.base.infrastructure.persistence.entity.pattern.PatternTemplateTestItemPojo;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.records.JPatternTemplateTestItemRecord;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Repository
public class PatternTemplateRepositoryCustomImpl implements PatternTemplateRepositoryCustom {

  @Autowired
  private DSLContext dslContext;

  @Override
  public int saveInBatch(List<PatternTemplateTestItemPojo> patternTemplateTestItems) {

    InsertValuesStep2<JPatternTemplateTestItemRecord, Long, Long> columns = dslContext.insertInto(
            PATTERN_TEMPLATE_TEST_ITEM)
        .columns(PATTERN_TEMPLATE_TEST_ITEM.PATTERN_ID, PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID);

    patternTemplateTestItems.forEach(
        pojo -> columns.values(pojo.getPatternTemplateId(), pojo.getTestItemId()));

    return columns.onConflictDoNothing().execute();
  }

  @Override
  public List<Long> findMatchedItemIdsIn(Long patternId, List<Long> itemId) {
    return dslContext.select(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID)
        .from(PATTERN_TEMPLATE_TEST_ITEM).where(PATTERN_TEMPLATE_TEST_ITEM.PATTERN_ID.eq(patternId))
        .and(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID.in(itemId))
        .fetchInto(Long.class);
  }
}
