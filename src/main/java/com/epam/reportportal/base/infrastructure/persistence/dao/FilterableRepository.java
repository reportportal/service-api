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

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Yauheni_Martynau
 */
public interface FilterableRepository<T> {

  /**
   * Executes query built for given filter
   *
   * @param filter Filter to build a query
   * @return List of mapped entries found
   */
  List<T> findByFilter(Queryable filter);

  /**
   * Executes query built for given filter and maps result for given page
   *
   * @param filter   Filter to build a query
   * @param pageable {@link Pageable}
   * @return List of mapped entries found
   */
  Page<T> findByFilter(Queryable filter, Pageable pageable);
}
