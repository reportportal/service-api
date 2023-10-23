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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.AnalyzeLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;
import com.epam.ta.reportportal.ws.model.launch.cluster.CreateClustersRQ;
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
   * @param launchId       ID of Launch object
   * @param projectDetails Project Details
   * @param user           Recipient user
   * @param rq             Request Data
   * @return OperationCompletionRS - Response Data
   */
  OperationCompletionRS updateLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user,
      UpdateLaunchRQ rq);

  /**
   * Start launch analyzer on demand
   *
   * @param projectDetails  Project Details
   * @param analyzeLaunchRQ Launch analyze rq
   * @param user            User started analysis
   * @return OperationCompletionRS - Response Data
   */
  OperationCompletionRS startLaunchAnalyzer(AnalyzeLaunchRQ analyzeLaunchRQ,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user);

  OperationCompletionRS createClusters(CreateClustersRQ createClustersRQ,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user);

  /**
   * Bulk launch update.
   *
   * @param rq             Bulk request
   * @param projectDetails Project Details
   * @param user           User
   * @return OperationCompletionRS
   */
  List<OperationCompletionRS> updateLaunch(BulkRQ<Long, UpdateLaunchRQ> rq,
      ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user);

  OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq,
      ReportPortalUser.ProjectDetails projectDetails);
}