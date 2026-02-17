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

package com.epam.reportportal.base.core.item;

import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.issue.DefineIssueRQ;
import com.epam.reportportal.base.model.item.ExternalIssueRQ;
import com.epam.reportportal.base.model.item.UpdateTestItemRQ;
import com.epam.reportportal.base.reporting.BulkInfoUpdateRQ;
import com.epam.reportportal.base.reporting.Issue;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
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
   * @param membershipDetails Membership details
   * @param defineIssue       issues request data
   * @param user              user
   * @return list of defined issues for specified test items
   */
  List<Issue> defineTestItemsIssues(MembershipDetails membershipDetails,
      DefineIssueRQ defineIssue, ReportPortalUser user);

  /**
   * Update specified test item
   *
   * @param membershipDetails Membership details
   * @param itemId            test item ID
   * @param rq                update test item request data
   * @param user              request principal name
   * @return OperationCompletionRS
   */
  OperationCompletionRS updateTestItem(MembershipDetails membershipDetails, Long itemId,
      UpdateTestItemRQ rq, ReportPortalUser user);

  /**
   * Add or remove external system issue link directly to the {@link TestItem}
   *
   * @param request           {@link ExternalIssueRQ}
   * @param membershipDetails {@link MembershipDetails}
   * @param user              {@link ReportPortalUser}
   * @return {@link List} of the {@link OperationCompletionRS}
   */
  List<OperationCompletionRS> processExternalIssues(ExternalIssueRQ request,
      MembershipDetails membershipDetails, ReportPortalUser user);

  /**
   * Resets items issue to default state
   *
   * @param itemIds   The {@link List} of the {@link TestItemResults#itemId}
   * @param projectId Project id
   * @param userId    User ID
   * @param userLogin User login
   */
  void resetItemsIssue(List<Long> itemIds, Long projectId, Long userId, String userLogin);

  OperationCompletionRS bulkInfoUpdate(BulkInfoUpdateRQ bulkUpdateRq,
      MembershipDetails membershipDetails, ReportPortalUser user);
}
