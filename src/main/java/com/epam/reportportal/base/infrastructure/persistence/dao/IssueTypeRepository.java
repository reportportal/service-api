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

import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence for global {@link IssueType} definitions and custom sub-types.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public interface IssueTypeRepository extends ReportPortalRepository<IssueType, Long>,
    IssueTypeRepositoryCustom {

  /**
   * Find issue type by it's locator
   *
   * @param locator locator
   * @return Optional of IssueType
   */
  Optional<IssueType> findByLocator(String locator);

  @Query(value = """
              SELECT it.* from issue_type it 
                  join public.issue_group ig on it.issue_group_id = ig.issue_group_id 
                  where it.locator in (:locators)
      """,
      nativeQuery = true)
  List<IssueType> getDefaultIssueTypes(@Param("locators") List<String> locators);
}
