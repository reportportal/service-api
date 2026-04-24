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

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.GLOBAL_INTEGRATION_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.RecordMappers.PROJECT_INTEGRATION_RECORD_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.INTEGRATION_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JIntegration.INTEGRATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JIntegrationType.INTEGRATION_TYPE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Implements {@link IntegrationRepositoryCustom}.
 *
 * @author Yauheni_Martynau
 */
@Repository
public class IntegrationRepositoryCustomImpl implements IntegrationRepositoryCustom {

  private final DSLContext dsl;

  @Autowired
  public IntegrationRepositoryCustomImpl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  public List<Integration> findByFilter(Queryable filter) {
    return INTEGRATION_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter).wrap().build()));
  }

  @Override
  public Page<Integration> findByFilter(Queryable filter, Pageable pageable) {
    return PageableExecutionUtils.getPage(
        INTEGRATION_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter)
            .with(pageable)
            .wrap()
            .withWrapperSort(pageable.getSort())
            .build())), pageable, () -> dsl.fetchCount(QueryBuilder.newBuilder(filter).build()));
  }

  @Override
  public Optional<Integration> findGlobalById(Long integrationId) {
    return ofNullable(dsl.select()
        .from(INTEGRATION)
        .join(INTEGRATION_TYPE)
        .on(INTEGRATION.TYPE.eq(INTEGRATION_TYPE.ID))
        .where(INTEGRATION.ID.eq(integrationId.intValue()).and(INTEGRATION.PROJECT_ID.isNull()))
        .fetchAny(GLOBAL_INTEGRATION_RECORD_MAPPER));
  }

  @Override
  public List<Integration> findAllByProjectIdAndInIntegrationTypeIds(Long projectId,
      List<Long> integrationTypeIds) {
    return dsl.select()
        .from(INTEGRATION)
        .join(INTEGRATION_TYPE)
        .on(INTEGRATION.TYPE.eq(INTEGRATION_TYPE.ID))
        .where(INTEGRATION_TYPE.ID.in(integrationTypeIds))
        .and(INTEGRATION.PROJECT_ID.eq(projectId))
        .orderBy(INTEGRATION.CREATION_DATE.desc())
        .fetch(PROJECT_INTEGRATION_RECORD_MAPPER);
  }

  @Override
  public List<Integration> findAllGlobalInIntegrationTypeIds(List<Long> integrationTypeIds) {
    return dsl.select()
        .from(INTEGRATION)
        .join(INTEGRATION_TYPE)
        .on(INTEGRATION.TYPE.eq(INTEGRATION_TYPE.ID))
        .where(INTEGRATION_TYPE.ID.in(integrationTypeIds))
        .and(INTEGRATION.PROJECT_ID.isNull())
        .orderBy(INTEGRATION.CREATION_DATE.desc())
        .fetch(GLOBAL_INTEGRATION_RECORD_MAPPER);
  }

  @Override
  public List<Integration> findAllGlobalNotInIntegrationTypeIds(List<Long> integrationTypeIds) {
    return dsl.select()
        .from(INTEGRATION)
        .join(INTEGRATION_TYPE)
        .on(INTEGRATION.TYPE.eq(INTEGRATION_TYPE.ID))
        .where(INTEGRATION_TYPE.ID.notIn(integrationTypeIds))
        .and(INTEGRATION.PROJECT_ID.isNull())
        .orderBy(INTEGRATION.CREATION_DATE.desc())
        .fetch(GLOBAL_INTEGRATION_RECORD_MAPPER);
  }
}
