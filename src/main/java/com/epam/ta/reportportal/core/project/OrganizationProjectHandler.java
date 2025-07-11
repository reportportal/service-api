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

package com.epam.ta.reportportal.core.project;

import com.epam.reportportal.api.model.OrganizationProjectsPage;
import com.epam.reportportal.api.model.ProjectBase;
import com.epam.reportportal.api.model.ProjectInfo;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import org.springframework.data.domain.Pageable;

public interface OrganizationProjectHandler {

  /**
   * Returns a page of projects for the organization based on the provided filter and pagination.
   *
   * @param orgId    the id of the organization whose projects are queried
   * @param filter   the {@link Filter} with condition(s) to be applied on the project querying
   * @param pageable the {@link Pageable} to define the pagination details for the result
   * @return an {@link OrganizationProjectsPage} represents a page of projects for the organization
   */
  OrganizationProjectsPage getOrganizationProjectsPage(Long orgId, Queryable filter, Pageable pageable);

  ProjectInfo createProject(Long orgId, ProjectBase projectDetails);

  void deleteProject(Long orgId, Long prjId);
}
