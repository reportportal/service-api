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

package com.epam.reportportal.base.core.dashboard;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.dashboard.CreateDashboardRQ;

/**
 * @author Pavel Bortnik
 */
public interface CreateDashboardHandler {

  /**
   * Creates a new dashboard.
   *
   * @param membershipDetails Membership details
   * @param rq                Dashboard details
   * @param user              User
   * @return EntryCreatedRS
   */
  EntryCreatedRS createDashboard(MembershipDetails membershipDetails,
      CreateDashboardRQ rq, ReportPortalUser user);

}
