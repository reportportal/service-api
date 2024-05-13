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

package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.log.impl.PagedLogResource;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.log.GetLogsUnderRq;
import com.epam.ta.reportportal.model.log.LogResource;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * GET operation for {@link Log} entity
 *
 * @author Andrei Varabyeu
 */
public interface GetLogHandler {

  /**
   * Returns logs for specified filter
   *
   * @param filterable     - filter definition
   * @param pageable       - pageable definition
   * @param path           - logs path
   * @param membershipDetails Membership details
   * @return mapping with {@link TestItem#getItemId()} as key and its list of {@link LogResource} as value
   */
  Iterable<LogResource> getLogs(String path, MembershipDetails membershipDetails,
      Filter filterable, Pageable pageable);

  /**
   * @param logsUnderRq    {@link GetLogsUnderRq}
   * @param membershipDetails {@link MembershipDetails}
   * @return mapping with {@link TestItem#getItemId()} as key and its {@link Log} list as value
   */
  Map<Long, List<LogResource>> getLogs(GetLogsUnderRq logsUnderRq,
      MembershipDetails membershipDetails);

  /**
   * Returns log by UUID
   *
   * @param logId          - target log UUID value
   * @param membershipDetails Membership details
   * @param user           User
   * @return LogResource
   */
  LogResource getLog(String logId, MembershipDetails membershipDetails,
      ReportPortalUser user);

  /**
   * Calculates page number and returns entire page for specified log ID
   *
   * @param logId          ID of log to find
   * @param membershipDetails Membership details
   * @param filterable     Filter for paging
   * @param pageable       Paging details
   * @return Page Number
   */
  long getPageNumber(Long logId, MembershipDetails membershipDetails, Filter filterable,
      Pageable pageable);

  /**
   * Get logs and nested steps as one collection, filtered and sorted by passed args
   *
   * @param parentId       {@link com.epam.ta.reportportal.entity.log.Log#testItem} ID or
   *                       {@link com.epam.ta.reportportal.entity.item.TestItem#parentId} ID
   * @param membershipDetails {@link MembershipDetails}
   * @param params         Request params
   * @param queryable      {@link Queryable}
   * @param pageable       {@link Pageable}
   * @return The {@link Iterable} of {@link LogResource} and
   * {@link com.epam.ta.reportportal.model.NestedStepResource} entities
   */
  Iterable<?> getNestedItems(Long parentId, MembershipDetails membershipDetails,
      Map<String, String> params, Queryable queryable, Pageable pageable);

  List<PagedLogResource> getLogsWithLocation(Long parentId,
      MembershipDetails membershipDetails, Map<String, String> params,
      Queryable queryable, Pageable pageable);
}
