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
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.model.dashboard.UpdateDashboardRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;

/**
 * @author Pavel Bortnik
 */
public interface UpdateDashboardHandler {

  /**
   * Update dashboard with specified id
   *
   * @param membershipDetails Membership details
   * @param rq             Update details
   * @param dashboardId    Dashboard id to be updated
   * @param user           User
   * @return OperationCompletionRS
   */
  OperationCompletionRS updateDashboard(MembershipDetails membershipDetails,
      UpdateDashboardRQ rq, Long dashboardId,
      ReportPortalUser user);

  /**
   * Add a new widget to the specified dashboard
   *
   * @param dashboardId    Dashboard id
   * @param membershipDetails Membership details
   * @param rq             Widget details
   * @param user           User
   * @return OperationCompletionRS
   */
  OperationCompletionRS addWidget(Long dashboardId, MembershipDetails membershipDetails,
      AddWidgetRq rq,
      ReportPortalUser user);

  /**
   * Removes a specified widget from the specified dashboard
   *
   * @param widgetId       Widget id
   * @param dashboardId    Dashboard id
   * @param membershipDetails Membership details
   * @param user           {@link ReportPortalUser}
   * @return OperationCompletionRS
   */
  OperationCompletionRS removeWidget(Long widgetId, Long dashboardId,
      MembershipDetails membershipDetails,
      ReportPortalUser user);

}
