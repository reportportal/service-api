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
import com.epam.ta.reportportal.model.issue.DefineIssueRQ;
import com.epam.ta.reportportal.model.item.ExternalIssueRQ;
import com.epam.ta.reportportal.model.item.UpdateTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.BulkInfoUpdateRQ;
import com.epam.ta.reportportal.ws.reporting.Issue;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.List;

/**
 * Handler to update test item issue type and issue statistics
 *
 * @author Dzianis Shlychkou
 */
public interface UpdateTestItemHandler {

  /**
   * Define TestItem issue (or list of issues)
   *
   * @param projectDetails Project Details
   * @param defineIssue    issues request data
   * @param user           user
   * @return list of defined issues for specified test items
   */
  List<Issue> defineTestItemsIssues(MembershipDetails membershipDetails,
      DefineIssueRQ defineIssue, ReportPortalUser user);

  /**
   * Update specified test item
   *
   * @param projectDetails Project Details
   * @param itemId         test item ID
   * @param rq             update test item request data
   * @param user           request principal name
   * @return OperationCompletionRS
   */
  OperationCompletionRS updateTestItem(MembershipDetails membershipDetails, Long itemId,
      UpdateTestItemRQ rq, ReportPortalUser user);

  /**
   * Add or remove external system issue link directly to the
   * {@link com.epam.ta.reportportal.entity.item.TestItem}
   *
   * @param request        {@link ExternalIssueRQ}
   * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
   * @param user           {@link ReportPortalUser}
   * @return {@link List} of the {@link OperationCompletionRS}
   */
  List<OperationCompletionRS> processExternalIssues(ExternalIssueRQ request,
      MembershipDetails membershipDetails, ReportPortalUser user);

  /**
   * Resets items issue to default state
   *
   * @param itemIds   The {@link List} of the
   *                  {@link com.epam.ta.reportportal.entity.item.TestItemResults#itemId}
   * @param projectId Project id
   * @param user      {@link ReportPortalUser}
   */
  void resetItemsIssue(List<Long> itemIds, Long projectId, ReportPortalUser user);

  OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq,
      MembershipDetails membershipDetails);
}
