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

package com.epam.ta.reportportal.core.organization;

import com.epam.reportportal.api.model.OrganizationInfo;
import com.epam.reportportal.api.model.OrganizationPage;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.model.organization.OrganizationResource;
import java.io.OutputStream;
import org.springframework.data.domain.Pageable;

/**
 * Handler interface for retrieving and exporting organization information.
 *
 * @author Andrei Piankouski
 */
public interface GetOrganizationHandler {

  /**
   * Get Organization resource information.
   *
   * @param organizationId Organization id
   * @return {@link OrganizationResource}
   */
  OrganizationInfo getOrganizationById(Long organizationId);

  /**
   * Get Organizations by query parameters.
   *
   * @param filter   Queryable filter to apply on organizations
   * @param pageable Pagination information for the results
   * @return An {@link Iterable} of {@link OrganizationResource} containing information about all projects
   */
  OrganizationPage getOrganizations(Queryable filter, Pageable pageable);

  /**
   * Export organizations matching the given filter to the provided output stream.
   *
   * @param filter       Queryable filter to apply on organizations
   * @param pageable     Pagination information for the results
   * @param outputStream OutputStream to write the exported data
   */
  void exportOrganizations(Queryable filter, Pageable pageable, ReportFormat reportFormat, OutputStream outputStream);
}
