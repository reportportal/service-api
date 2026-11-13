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

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.ACTIVITY_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.ACTIVITY_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JActivity.ACTIVITY;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Implements {@link ActivityRepositoryCustom}.
 *
 * @author Ihar Kahadouski
 */
@Repository
public class ActivityRepositoryCustomImpl implements ActivityRepositoryCustom {

  private final DSLContext dsl;

  @Autowired
  public ActivityRepositoryCustomImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public void deleteModifiedLaterAgo(Long projectId, Duration period) {
    Instant bound = Instant.now().minus(period);
    dsl.delete(ACTIVITY).where(ACTIVITY.PROJECT_ID.eq(projectId))
        .and(ACTIVITY.CREATED_AT.lt(bound)).execute();
  }

  @Override
  public List<Activity> findByFilterWithSortingAndLimit(Queryable filter, Sort sort, int limit) {
    return dsl.fetch(QueryBuilder.newBuilder(filter).with(sort).with(limit).wrap().build())
        .map(ACTIVITY_MAPPER);
  }

  @Override
  public List<Activity> findByFilter(Queryable filter) {
    return ACTIVITY_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter).wrap().build()));
  }

  @Override
  public Page<Activity> findByFilter(Queryable filter, Pageable pageable) {
    return PageableExecutionUtils.getPage(
        ACTIVITY_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter)
            .with(pageable)
            .wrap()
            .withWrapperSort(pageable.getSort())
            .build())), pageable, () -> dsl.fetchCount(QueryBuilder.newBuilder(filter).build()));
  }
}
