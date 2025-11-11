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

package com.epam.reportportal.infrastructure.persistence.dao;

import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.ID;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.KEY;
import static com.epam.reportportal.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.LAUNCHES;
import static com.epam.reportportal.infrastructure.persistence.dao.util.JooqFieldNameTransformer.fieldName;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.ITEM_ATTRIBUTE;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.TEST_ITEM;
import static org.jooq.impl.DSL.not;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.infrastructure.persistence.dao.util.QueryUtils;
import com.epam.reportportal.infrastructure.persistence.entity.item.ItemAttributePojo;
import com.epam.reportportal.infrastructure.persistence.jooq.tables.records.JItemAttributeRecord;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep4;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Repository
public class ItemAttributeRepositoryCustomImpl implements ItemAttributeRepositoryCustom {

  public static final Integer ATTRIBUTES_LIMIT = 50;
  private static final int TIMEOUT_SEC = 10;

  private final DSLContext dslContext;

  @Autowired
  public ItemAttributeRepositoryCustomImpl(DSLContext dslContext) {
    this.dslContext = dslContext;
  }

  @Override
  public List<String> findAllKeysByLaunchFilter(Queryable launchFilter, Pageable launchPageable,
      boolean isLatest, String keyPart,
      boolean isSystem) {

    return dslContext.select(fieldName(KEY))
        .from(dslContext.with(LAUNCHES)
            .as(QueryUtils.createQueryBuilderWithLatestLaunchesOption(launchFilter,
                    launchPageable.getSort(), isLatest)
                .with(launchPageable)
                .build())
            .selectDistinct(ITEM_ATTRIBUTE.KEY)
            .from(ITEM_ATTRIBUTE)
            .join(TEST_ITEM)
            .on(ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID)
                .and(TEST_ITEM.HAS_STATS)
                .and(TEST_ITEM.HAS_CHILDREN.isFalse())
                .and(TEST_ITEM.RETRY_OF.isNull()))
            .join(LAUNCHES)
            .on(TEST_ITEM.LAUNCH_ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
            .where(ITEM_ATTRIBUTE.SYSTEM.isFalse())
            .and(ITEM_ATTRIBUTE.KEY.likeIgnoreCase(DSL.val("%" + DSL.escape(keyPart, '\\') + "%")))
            .unionAll(dslContext.selectDistinct(ITEM_ATTRIBUTE.KEY)
                .from(ITEM_ATTRIBUTE)
                .join(LAUNCHES)
                .on(ITEM_ATTRIBUTE.LAUNCH_ID.eq(fieldName(LAUNCHES, ID).cast(Long.class)))
                .where(ITEM_ATTRIBUTE.SYSTEM.isFalse())
                .and(ITEM_ATTRIBUTE.KEY.likeIgnoreCase(
                    DSL.val("%" + DSL.escape(keyPart, '\\') + "%")))))
        .groupBy(fieldName(KEY))
        .orderBy(DSL.length(fieldName(KEY).cast(String.class)))
        .limit(ATTRIBUTES_LIMIT)
        .fetchInto(String.class);
  }

  @Override
  public List<String> findLaunchAttributeKeys(Long projectId, String value, boolean system) {
    return dslContext.selectDistinct(ITEM_ATTRIBUTE.KEY)
        .from(ITEM_ATTRIBUTE)
        .leftJoin(LAUNCH)
        .on(ITEM_ATTRIBUTE.LAUNCH_ID.eq(LAUNCH.ID))
        .leftJoin(PROJECT)
        .on(LAUNCH.PROJECT_ID.eq(PROJECT.ID))
        .where(PROJECT.ID.eq(projectId))
        .and(ITEM_ATTRIBUTE.SYSTEM.eq(system))
        .and(ITEM_ATTRIBUTE.KEY.likeIgnoreCase("%" + DSL.escape(value, '\\') + "%"))
        .fetch(ITEM_ATTRIBUTE.KEY);
  }

  @Override
  public List<String> findLaunchAttributeValues(Long projectId, String key, String value,
      boolean system) {
    Condition condition = prepareFetchingValuesCondition(PROJECT.ID, projectId, key, value, system);
    return dslContext.selectDistinct(ITEM_ATTRIBUTE.VALUE)
        .from(ITEM_ATTRIBUTE)
        .leftJoin(LAUNCH)
        .on(ITEM_ATTRIBUTE.LAUNCH_ID.eq(LAUNCH.ID))
        .leftJoin(PROJECT)
        .on(LAUNCH.PROJECT_ID.eq(PROJECT.ID))
        .where(condition)
        .fetch(ITEM_ATTRIBUTE.VALUE);
  }

  @Override
  public List<String> findUniqueAttributeKeysByPart(Long projectId, String keyPart, Long launchId,
      boolean system) {
    SelectConditionStep<Record1<String>> conditionStep = dslContext.selectDistinct(
            ITEM_ATTRIBUTE.KEY)
        .from(ITEM_ATTRIBUTE)
        .leftJoin(TEST_ITEM)
        .on(ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
        .leftJoin(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(LAUNCH.PROJECT_ID.eq(projectId))
        .and(ITEM_ATTRIBUTE.KEY.likeIgnoreCase("%" + DSL.escape(keyPart, '\\') + "%"))
        .and(ITEM_ATTRIBUTE.SYSTEM.eq(system));
    if (launchId != null) {
      conditionStep = conditionStep.and(LAUNCH.ID.eq(launchId));
    }
    return conditionStep.queryTimeout(TIMEOUT_SEC).fetch(ITEM_ATTRIBUTE.KEY);
  }

  @Override
  public List<String> findUniqueAttributeValuesByPart(Long projectId, String key, String valuePart,
      Long launchId, boolean system) {
    SelectConditionStep<Record1<String>> condition = dslContext.selectDistinct(
            ITEM_ATTRIBUTE.VALUE)
        .from(ITEM_ATTRIBUTE)
        .leftJoin(TEST_ITEM)
        .on(ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
        .leftJoin(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(LAUNCH.PROJECT_ID.eq(projectId))
        .and(ITEM_ATTRIBUTE.VALUE.likeIgnoreCase("%" + DSL.escape(valuePart, '\\') + "%"))
        .and(ITEM_ATTRIBUTE.SYSTEM.eq(system));
    if (launchId != null) {
      condition = condition.and(LAUNCH.ID.eq(launchId));
    }
    if (StringUtils.hasText(key)) {
      condition = condition.and(ITEM_ATTRIBUTE.KEY.eq(key));
    }
    return condition.queryTimeout(TIMEOUT_SEC).fetch(ITEM_ATTRIBUTE.VALUE);
  }

  @Override
  public List<String> findTestItemAttributeValues(Long launchId, String key, String value,
      boolean system) {
    Condition condition = prepareFetchingValuesCondition(LAUNCH.ID, launchId, key, value, system);
    return dslContext.selectDistinct(ITEM_ATTRIBUTE.VALUE)
        .from(ITEM_ATTRIBUTE)
        .leftJoin(TEST_ITEM)
        .on(ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
        .leftJoin(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(condition)
        .fetch(ITEM_ATTRIBUTE.VALUE);
  }

  @Override
  public List<String> findTestItemKeysByProjectIdAndLaunchName(Long projectId, String launchName,
      String keyPart, boolean system) {
    return dslContext.selectDistinct(ITEM_ATTRIBUTE.KEY)
        .from(ITEM_ATTRIBUTE)
        .join(TEST_ITEM)
        .on(ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
        .join(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(LAUNCH.PROJECT_ID.eq(projectId))
        .and(LAUNCH.NAME.eq(launchName))
        .and(TEST_ITEM.HAS_STATS)
        .and(not(TEST_ITEM.HAS_CHILDREN))
        .and(ITEM_ATTRIBUTE.SYSTEM.eq(system))
        .and(ITEM_ATTRIBUTE.KEY.likeIgnoreCase("%" + DSL.escape(keyPart, '\\') + "%"))
        .fetch(ITEM_ATTRIBUTE.KEY);
  }

  @Override
  public List<String> findTestItemValuesByProjectIdAndLaunchName(Long projectId, String launchName,
      String key, String valuePart,
      boolean system) {
    Condition condition = prepareFetchingValuesCondition(LAUNCH.PROJECT_ID, projectId, key,
        valuePart, system);
    return dslContext.selectDistinct(ITEM_ATTRIBUTE.VALUE)
        .from(ITEM_ATTRIBUTE)
        .join(TEST_ITEM)
        .on(ITEM_ATTRIBUTE.ITEM_ID.eq(TEST_ITEM.ITEM_ID))
        .join(LAUNCH)
        .on(TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID))
        .where(condition)
        .and(LAUNCH.NAME.eq(launchName))
        .and(TEST_ITEM.HAS_STATS)
        .and(not(TEST_ITEM.HAS_CHILDREN))
        .fetch(ITEM_ATTRIBUTE.VALUE);
  }

  @Override
  public int saveByItemId(Long itemId, String key, String value, boolean isSystem) {
    return dslContext.insertInto(ITEM_ATTRIBUTE)
        .columns(ITEM_ATTRIBUTE.KEY, ITEM_ATTRIBUTE.VALUE, ITEM_ATTRIBUTE.ITEM_ID,
            ITEM_ATTRIBUTE.SYSTEM)
        .values(key, value, itemId, isSystem)
        .execute();
  }

  @Override
  public int saveByLaunchId(Long launchId, String key, String value, boolean isSystem) {
    return dslContext.insertInto(ITEM_ATTRIBUTE)
        .columns(ITEM_ATTRIBUTE.KEY, ITEM_ATTRIBUTE.VALUE, ITEM_ATTRIBUTE.LAUNCH_ID,
            ITEM_ATTRIBUTE.SYSTEM)
        .values(key, value, launchId, isSystem)
        .execute();
  }

  @Override
  public int saveMultiple(List<ItemAttributePojo> itemAttributes) {

    InsertValuesStep4<JItemAttributeRecord, Long, String, String, Boolean> columns = dslContext.insertInto(
            ITEM_ATTRIBUTE)
        .columns(ITEM_ATTRIBUTE.ITEM_ID, ITEM_ATTRIBUTE.KEY, ITEM_ATTRIBUTE.VALUE,
            ITEM_ATTRIBUTE.SYSTEM);

    itemAttributes.forEach(
        pojo -> columns.values(pojo.getItemId(), pojo.getKey(), pojo.getValue(), pojo.isSystem()));

    return columns.execute();
  }

  private Condition prepareFetchingValuesCondition(TableField<? extends Record, Long> field,
      Long id, String key, String value,
      boolean system) {
    Condition condition = field.eq(id)
        .and(ITEM_ATTRIBUTE.SYSTEM.eq(system))
        .and(ITEM_ATTRIBUTE.VALUE.likeIgnoreCase(
            "%" + (value == null ? "" : DSL.escape(value, '\\') + "%")));
    if (key != null) {
      condition = condition.and(ITEM_ATTRIBUTE.KEY.eq(key));
    }
    return condition;
  }

}
