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
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.util.Collection;
import java.util.List;

/**
 * Handler for delete test item operation
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
public interface DeleteTestItemHandler {

  /**
   * Delete test item by id.
   *
   * @param itemId         Item id
   * @param membershipDetails Membership details
   * @param user           User
   * @return {@link OperationCompletionRS}
   */
  OperationCompletionRS deleteTestItem(Long itemId, MembershipDetails membershipDetails,
      ReportPortalUser user);

  /**
   * Delete list of items by ids.
   *
   * @param ids            Test item ids
   * @param membershipDetails Membership details
   * @param user           User
   * @return {@link OperationCompletionRS}
   */
  List<OperationCompletionRS> deleteTestItems(Collection<Long> ids,
      MembershipDetails membershipDetails,
      ReportPortalUser user);
}
