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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.query.JoinEntity;
import java.util.List;
import java.util.function.Supplier;
import org.jooq.Field;

/**
 * Fluent builder for {@link CriteriaHolder} entries used in dynamic filters.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */

public class CriteriaHolderBuilder implements Supplier<CriteriaHolder> {

  private CriteriaHolder criteriaHolder;

  public CriteriaHolderBuilder newBuilder(String filterCriteria, String queryCriteria,
      Class<?> dataType) {
    this.criteriaHolder = new CriteriaHolder(filterCriteria, queryCriteria, dataType);
    return this;
  }

  public CriteriaHolderBuilder newBuilder(String filterCriteria, String queryCriteria,
      Class<?> dataType, List<JoinEntity> joinChain) {
    this.criteriaHolder = new CriteriaHolder(filterCriteria, queryCriteria, dataType, joinChain);
    return this;
  }

  public CriteriaHolderBuilder newBuilder(String filterCriteria, Field queryCriteria,
      Class<?> dataType) {
    this.criteriaHolder = new CriteriaHolder(filterCriteria, queryCriteria, dataType);
    return this;
  }

  public CriteriaHolderBuilder newBuilder(String filterCriteria, Field queryCriteria,
      Class<?> dataType, List<JoinEntity> joinChain) {
    this.criteriaHolder = new CriteriaHolder(filterCriteria, queryCriteria, dataType, joinChain);
    return this;
  }

  public CriteriaHolderBuilder withAggregateCriteria(String aggregateCriteria) {
    this.criteriaHolder.setAggregateCriteria(aggregateCriteria);
    return this;
  }

  @Override
  public CriteriaHolder get() {
    return criteriaHolder;
  }
}
