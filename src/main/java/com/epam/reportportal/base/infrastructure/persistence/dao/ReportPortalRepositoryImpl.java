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

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import jakarta.persistence.EntityManager;
import java.io.Serializable;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link ReportPortalRepository} (refresh and filter-based exists).
 *
 * @param <T>  entity type
 * @param <ID> primary key type
 * @author Pavel Bortnik
 */
public class ReportPortalRepositoryImpl<T, ID extends Serializable> extends
    SimpleJpaRepository<T, ID>
    implements ReportPortalRepository<T, ID> {

  private final EntityManager entityManager;

  private DSLContext dsl;

  public ReportPortalRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
      EntityManager entityManager) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
  }

  @Autowired
  public void setDsl(DSLContext dsl) {
    this.dsl = dsl;
  }

  @Override
  @Transactional
  public void refresh(T t) {
    entityManager.refresh(t);
  }

  @Override
  public boolean exists(Filter filter) {
    return dsl.fetchExists(filter.toQuery().get());
  }
}
