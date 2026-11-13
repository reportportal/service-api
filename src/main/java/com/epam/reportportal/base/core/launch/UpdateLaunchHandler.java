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

package com.epam.reportportal.base.core.launch;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.BulkRQ;
import com.epam.reportportal.base.model.launch.AnalyzeLaunchRQ;
import com.epam.reportportal.base.model.launch.UpdateLaunchRQ;
import com.epam.reportportal.base.model.launch.cluster.CreateClustersRQ;
import com.epam.reportportal.base.reporting.BulkInfoUpdateRQ;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import java.util.List;

/**
 * Update launch operation handler
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface UpdateLaunchHandler {

  /**
   * Update specified by id launch.
   *
   * @param launchId          ID of Launch object
   * @param membershipDetails Membership details
   * @param user              Recipient user
   * @param rq                Request Data
   * @return OperationCompletionRS - Response Data
   */
  OperationCompletionRS updateLaunch(Long launchId, MembershipDetails membershipDetails,
      ReportPortalUser user,
      UpdateLaunchRQ rq);

  /**
   * Start launch analyzer on demand
   *
   * @param membershipDetails Membership details
   * @param analyzeLaunchRQ   Launch analyze rq
   * @param user              User started analysis
   * @return OperationCompletionRS - Response Data
   */
  OperationCompletionRS startLaunchAnalyzer(AnalyzeLaunchRQ analyzeLaunchRQ,
      MembershipDetails membershipDetails,
      ReportPortalUser user);

  OperationCompletionRS createClusters(CreateClustersRQ createClustersRQ,
      MembershipDetails membershipDetails,
      ReportPortalUser user);

  /**
   * Bulk launch update.
   *
   * @param rq                Bulk request
   * @param membershipDetails Membership details
   * @param user              User
   * @return OperationCompletionRS
   */
  List<OperationCompletionRS> updateLaunch(BulkRQ<Long, UpdateLaunchRQ> rq,
      MembershipDetails membershipDetails,
      ReportPortalUser user);

  OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq,
      MembershipDetails membershipDetails);
}
