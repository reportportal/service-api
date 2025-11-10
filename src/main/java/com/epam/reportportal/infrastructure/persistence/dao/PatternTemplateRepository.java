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

import com.epam.reportportal.infrastructure.persistence.entity.pattern.PatternTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface PatternTemplateRepository extends ReportPortalRepository<PatternTemplate, Long>,
    PatternTemplateRepositoryCustom {

  Optional<PatternTemplate> findByIdAndProjectId(Long id, Long projectId);

  List<PatternTemplate> findAllByProjectIdAndEnabled(Long projectId, boolean enabled);

  boolean existsByProjectIdAndNameIgnoreCase(Long projectId, String name);

  /**
   * Required for regex validation on database level. Some regex patterns can be compiled by Java
   * {@link java.util.regex.Pattern} being incorrect for PostgreSQL regex syntax and vice versa
   *
   * @param regex Regex pattern
   */
  @Query(value = "SELECT '' ~ :regex", nativeQuery = true)
  void validateRegex(@Param("regex") String regex);
}
