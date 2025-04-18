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

package com.epam.ta.reportportal.core.dashboard;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.dashboard.DashboardConfigResource;
import com.epam.ta.reportportal.model.dashboard.DashboardResource;
import org.springframework.data.domain.Pageable;

/**
 * Get dashboard handler.
 *
 * @author Aliaksei_Makayed
 */
public interface GetDashboardHandler {

  /**
   * Get dashboard resource by provided id
   *
   * @param id             Provided id
   * @param projectDetails Project detail
   * @return {@link DashboardResource}
   */
  DashboardResource getDashboard(Long id, ReportPortalUser.ProjectDetails projectDetails);

  /**
   * Get permitted projects for concrete user for concrete project
   *
   * @param projectDetails Project details
   * @param user           User
   * @param pageable       Page Details
   * @param filter         {@link Filter}
   * @return Page of permitted dashboard resources
   */
  Page<DashboardResource> getDashboards(ReportPortalUser.ProjectDetails projectDetails,
                                        Pageable pageable, Filter filter,
                                        ReportPortalUser user);

  /**
   * Get Dashboard configuration including its widgets and filters if any
   *
   * @param id             Dashboard id
   * @param projectDetails Project details
   * @return Dashboard configuration
   */
  DashboardConfigResource getDashboardConfig(Long id,
      ReportPortalUser.ProjectDetails projectDetails);

}