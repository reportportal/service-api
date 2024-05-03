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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.ws.reporting.ItemCreatedRS;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;

/**
 * Handler for start launch operation
 *
 * @author Andrei Varabyeu
 */
public interface StartTestItemHandler {

  /**
   * Start Root item operation
   *
   * @param user           {@link ReportPortalUser}
   * @param projectDetails Project Details
   * @param rq             Item details
   * @return ItemID and uniqueID of test item
   */
  ItemCreatedRS startRootItem(ReportPortalUser user, MembershipDetails membershipDetails,
      StartTestItemRQ rq);

  /**
   * Start child item operation
   *
   * @param user           {@link ReportPortalUser}
   * @param projectDetails Project Details
   * @param rq             Item details
   * @param parentId       Id of parrent test item
   * @return ItemID and uniqueID of test item
   */
  ItemCreatedRS startChildItem(ReportPortalUser user,
      MembershipDetails membershipDetails, StartTestItemRQ rq, String parentId);
}
