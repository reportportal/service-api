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

import com.epam.reportportal.api.model.OrganizationProfile;
import com.epam.reportportal.api.model.OrganizationProfilesPage;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.model.organization.OrganizationResource;
import org.springframework.data.domain.Pageable;

/**
 * @author Andrei Piankouski
 */
public interface GetOrganizationHandler {

  /**
   * Get Organization resource information
   *
   * @param organizationId Organization id
   * @param user           User
   * @return {@link OrganizationResource}
   */
  OrganizationProfile getOrganizationById(Long organizationId, ReportPortalUser user);

  /**
   * Get Organizations by query parameters
   *
   * @param filter   Queryable filter to apply on organizations
   * @param pageable Pagination information for the results
   * @return An {@link Iterable} of {@link OrganizationResource} containing information about all
   * projects
   */
  OrganizationProfilesPage getOrganizations(ReportPortalUser rpUser, Queryable filter, Pageable pageable);

}
