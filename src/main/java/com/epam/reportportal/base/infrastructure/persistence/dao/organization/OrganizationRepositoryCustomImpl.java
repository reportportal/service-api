/*
 * Copyright 2024 EPAM Systems
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

package com.epam.reportportal.base.infrastructure.persistence.dao.organization;

import static com.epam.reportportal.base.infrastructure.persistence.dao.util.OrganizationMapper.ORGANIZATION_PROFILE_MAPPER;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.QueryUtils.collectJoinFields;
import static com.epam.reportportal.base.infrastructure.persistence.dao.util.ResultFetchers.ORGANIZATION_FETCHER;
import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationStatisticsFields.LAST_RUN;
import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationStatisticsFields.LAUNCHES_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationStatisticsFields.PROJECTS_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationStatisticsFields.USERS_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION_USER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JProject.PROJECT;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.Organization;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationProfile;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

/**
 * Repository implementation class for searching and filtering organization records.
 *
 * @author Siarhei Hrabko
 */
@Repository
public class OrganizationRepositoryCustomImpl implements OrganizationRepositoryCustom {

  protected final Logger LOGGER = LoggerFactory.getLogger(OrganizationRepositoryCustomImpl.class);

  @Autowired
  private DSLContext dsl;

  @Override
  public List<OrganizationProfile> findByFilter(Queryable filter) {

    SelectQuery<? extends Record> query = QueryBuilder
        .newBuilder(filter, collectJoinFields(filter))
        .build();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Find organizations query: {}", query);
    }
    return ORGANIZATION_FETCHER
        .apply(dsl.fetch(query));
  }

  @Override
  public Page<OrganizationProfile> findByFilter(Queryable filter, Pageable pageable) {
    SelectQuery<? extends Record> query = QueryBuilder.newBuilder(filter,
            collectJoinFields(filter, pageable.getSort()))
        .with(pageable)
        .build();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Find organizations with pagination query: {}", query);
    }
    return PageableExecutionUtils.getPage(ORGANIZATION_FETCHER
            .apply(dsl.fetch(query)),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter).build())
    );
  }

  @Override
  public Optional<OrganizationProfile> findOrganizationByIdAndUserId(Long orgId, Long userId) {
    var condition = ORGANIZATION.ID.eq(orgId);
    if (userId != null) {
      condition = condition.and(ORGANIZATION_USER.USER_ID.eq(userId));
    }
    return dsl.select(
            ORGANIZATION.ID,
            ORGANIZATION.NAME,
            ORGANIZATION.SLUG,
            ORGANIZATION.CREATED_AT,
            ORGANIZATION.UPDATED_AT,
            ORGANIZATION.EXTERNAL_ID,
            ORGANIZATION.ORGANIZATION_TYPE,
            ORGANIZATION.OWNER_ID,
            DSL.countDistinct(ORGANIZATION_USER.USER_ID).as(USERS_QUANTITY),
            DSL.countDistinct(PROJECT.ID).as(PROJECTS_QUANTITY),
            DSL.countDistinct(LAUNCH.ID).as(LAUNCHES_QUANTITY),
            DSL.max(LAUNCH.START_TIME).as(LAST_RUN))
        .from(ORGANIZATION)
        .leftJoin(ORGANIZATION_USER).on(ORGANIZATION_USER.ORGANIZATION_ID.eq(ORGANIZATION.ID))
        .leftJoin(PROJECT).on(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
        .leftJoin(LAUNCH).on(PROJECT.ID.eq(LAUNCH.PROJECT_ID).and(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS)))
        .where(condition)
        .groupBy(ORGANIZATION.ID)
        .fetchOptional(ORGANIZATION_PROFILE_MAPPER);
  }

  @Override
  public Optional<Organization> findById(Long orgId) {
    return dsl.select()
        .from(ORGANIZATION)
        .where(ORGANIZATION.ID.eq(orgId))
        .fetchOptionalInto(Organization.class);
  }

  @Override
  public Optional<Organization> findOrganizationByName(String name) {
    return dsl.select()
        .from(ORGANIZATION)
        .where(ORGANIZATION.NAME.eq(name))
        .fetchOptionalInto(Organization.class);
  }

  @Override
  public Optional<Organization> findOrganizationBySlug(String slug) {
    return dsl.select()
        .from(ORGANIZATION)
        .where(ORGANIZATION.SLUG.eq(slug))
        .fetchOptionalInto(Organization.class);
  }

}
