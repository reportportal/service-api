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

package com.epam.ta.reportportal.core.item.impl.history.provider;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.item.impl.history.param.HistoryRequestParams;
import com.epam.ta.reportportal.entity.item.history.TestItemHistory;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for {@link TestItemHistory} content providers
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public interface HistoryProvider {

  /**
   * @param filter               - {@link Queryable}
   * @param pageable             - {@link Pageable}
   * @param historyRequestParams - {@link HistoryRequestParams}
   * @param membershipDetails Membership details
   *                             {@link
   *                             MembershipDetails}
   * @param user                 - {@link ReportPortalUser}
   * @param usingHash            - true if need use hash
   * @return {@link Page} with {@link TestItemHistory} content
   */
  Page<TestItemHistory> provide(Queryable filter, Pageable pageable,
      HistoryRequestParams historyRequestParams,
      MembershipDetails membershipDetails, ReportPortalUser user, boolean usingHash);
}
