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


import static com.epam.reportportal.base.infrastructure.persistence.dao.util.OrganizationMapper.ORGANIZATION_USERS_LIST_FETCHER;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationUserAccount;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class OrganizationUsersRepositoryCustomImpl implements OrganizationUsersRepositoryCustom {

  protected final Logger LOGGER = LoggerFactory.getLogger(
      OrganizationUsersRepositoryCustomImpl.class);

  private final DSLContext dsl;

  @Override
  public List<OrganizationUserAccount> findByFilter(Queryable filter) {
    return ORGANIZATION_USERS_LIST_FETCHER.apply(dsl.fetch(QueryBuilder.newBuilder(filter)
        .build()));
  }

  @Override
  public Page<OrganizationUserAccount> findByFilter(Queryable filter, Pageable pageable) {
    SelectQuery<? extends Record> query = QueryBuilder.newBuilder(filter).with(pageable).build();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Find organization users query: {}", query);
    }
    return PageableExecutionUtils.getPage(
        ORGANIZATION_USERS_LIST_FETCHER.apply(dsl.fetch(query)),
        pageable,
        () -> dsl.fetchCount(QueryBuilder.newBuilder(filter).build()));
  }
}
