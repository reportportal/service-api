/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.filter;

import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.model.OwnedEntityResource;
import com.epam.reportportal.model.Page;
import com.epam.reportportal.model.filter.UserFilterResource;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Get filter handler
 *
 * @author Aliaksei_Makayed
 */
public interface GetUserFilterHandler {

  /**
   * Get {@link UserFilterResource} by provided id
   *
   * @param id                Provided id
   * @param membershipDetails Membership details
   * @return {@link UserFilterResource}
   */
  UserFilterResource getUserFilter(Long id, MembershipDetails membershipDetails);

  /**
   * Get {@link UserFilterResource} objects
   *
   * @param projectName Project Name
   * @param pageable    Page request
   * @param filter      Filter representation
   * @param user        ReportPortal User
   * @return {@link Page}
   */
  Page<UserFilterResource> getUserFilters(String projectName, Pageable pageable, Filter filter,
      ReportPortalUser user);

  /**
   * Get all {@link com.epam.reportportal.infrastructure.persistence.entity.filter.UserFilter}'s names
   *
   * @param membershipDetails Membership details
   * @param pageable          Page request
   * @param filter            Filter representation
   * @param user              ReportPortal user
   * @return List of {@link OwnedEntityResource}
   */
  Page<OwnedEntityResource> getFiltersNames(MembershipDetails membershipDetails,
      Pageable pageable, Filter filter, ReportPortalUser user);

  /**
   * Get all {@link UserFilterResource} objects
   *
   * @param ids               Filter IDs
   * @param membershipDetails Membership details
   * @param user              ReportPortal user
   * @return Found filters
   */
  List<UserFilter> getFiltersById(Long[] ids, MembershipDetails membershipDetails,
      ReportPortalUser user);
}
