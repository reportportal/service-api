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
import com.epam.ta.reportportal.model.Page;
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
   * @param projectDetails - project details
   * @return mapping with {@link TestItem#getItemId()} as key and its list of {@link LogResource} as value
   */
  Page<LogResource> getLogs(String path, ReportPortalUser.ProjectDetails projectDetails,
                            Filter filterable, Pageable pageable);

  /**
   * @param logsUnderRq    {@link GetLogsUnderRq}
   * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
   * @return mapping with {@link TestItem#getItemId()} as key and its {@link Log} list as value
   */
  Map<Long, List<LogResource>> getLogs(GetLogsUnderRq logsUnderRq,
      ReportPortalUser.ProjectDetails projectDetails);

  /**
   * Returns log by UUID
   *
   * @param logId          - target log UUID value
   * @param projectDetails Project details
   * @param user           User
   * @return LogResource
   */
  LogResource getLog(String logId, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user);

  /**
   * Calculates page number and returns entire page for specified log ID
   *
   * @param logId          ID of log to find
   * @param projectDetails Project details
   * @param filterable     Filter for paging
   * @param pageable       Paging details
   * @return Page Number
   */
  long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable,
      Pageable pageable);

  /**
   * Get logs and nested steps as one collection, filtered and sorted by passed args
   *
   * @param parentId       {@link com.epam.ta.reportportal.entity.log.Log#testItem} ID or
   *                       {@link com.epam.ta.reportportal.entity.item.TestItem#parentId} ID
   * @param projectDetails {@link com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails}
   * @param params         Request params
   * @param queryable      {@link Queryable}
   * @param pageable       {@link Pageable}
   * @return The {@link Iterable} of {@link LogResource} and
   * {@link com.epam.ta.reportportal.model.NestedStepResource} entities
   */
  Page<?> getNestedItems(Long parentId, ReportPortalUser.ProjectDetails projectDetails,
      Map<String, String> params, Queryable queryable, Pageable pageable);

  List<PagedLogResource> getLogsWithLocation(Long parentId,
      ReportPortalUser.ProjectDetails projectDetails, Map<String, String> params,
      Queryable queryable, Pageable pageable);
}
