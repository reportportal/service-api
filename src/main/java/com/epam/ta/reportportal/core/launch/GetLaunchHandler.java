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

import com.epam.reportportal.model.launch.cluster.ClusterInfoResource;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * Get Launch operation handler
 *
 * @author Andrei_Kliashchonak
 * @author Andrei Varabyeu
 */
public interface GetLaunchHandler {

  Launch get(Long id);

  /**
   * Get Launch resource by specified UUID
   *
   * @param launchId       Launch uuid
   * @param membershipDetails Membership details
   * @return {@link LaunchResource}
   */
  LaunchResource getLaunch(String launchId, MembershipDetails membershipDetails);

  /**
   * Get Launch resource by specified Name (for Jenkins Plugin)
   *
   * @param project  Project Name
   * @param pageable Page details
   * @param username User name
   * @param filter   {@link Filter}
   * @return Response Data
   */
  LaunchResource getLaunchByProjectKey(String project, Pageable pageable, Filter filter,
      String username);

  /**
   * Get list of Launch resources for specified project
   *
   * @param membershipDetails Membership details
   * @param filter         Filter data
   * @param pageable       Page details
   * @param userName       Name of User
   * @return Response Data
   */
  Page<LaunchResource> getProjectLaunches(MembershipDetails membershipDetails,
      Filter filter, Pageable pageable,
      String userName);

  /**
   * Get debug launches
   *
   * @param membershipDetails Membership details
   * @param filter         Filter data
   * @param pageable       Page details
   * @return Response Data
   */
  Page<LaunchResource> getDebugLaunches(MembershipDetails membershipDetails,
      Filter filter, Pageable pageable);

  /**
   * Get specified launch attribute keys (auto-complete functionality)
   *
   * @param membershipDetails Membership details
   * @param value          Tag prefix to be searched
   * @return List of found tags
   */
  List<String> getAttributeKeys(MembershipDetails membershipDetails, String value);

  /**
   * Get specified launch attribute values (auto-complete functionality)
   *
   * @param membershipDetails Membership details
   * @param value          Tag prefix to be searched
   * @param key            Attribute key
   * @return List of found tags
   */
  List<String> getAttributeValues(MembershipDetails membershipDetails, String key,
      String value);

  /**
   * Get launch names of specified project (auto-complete functionality)
   *
   * @param membershipDetails Membership details
   * @param value          Launch name prefix
   * @return List of found launches
   */
  List<String> getLaunchNames(MembershipDetails membershipDetails, String value);

  /**
   * Get unique owners of launches in specified mode
   *
   * @param membershipDetails Membership details
   * @param value          Owner name prefix
   * @param mode           Mode
   * @return Response Data
   */
  List<String> getOwners(MembershipDetails membershipDetails, String value, String mode);

  /**
   * Get launches comparison info
   *
   * @param membershipDetails Membership details
   * @param ids            IDs to be looked up
   * @return Response Data //
   */
  Map<String, List<ChartStatisticsContent>> getLaunchesComparisonInfo(
      MembershipDetails membershipDetails, Long[] ids);

  /**
   * Get statuses of specified launches
   *
   * @param membershipDetails Membership details
   * @param ids            Launch IDs
   * @return Response Data
   */
  Map<String, String> getStatuses(MembershipDetails membershipDetails, Long[] ids);

  /**
   * Exports the launch report in the specified format and writes it to the HTTP response.
   * <p>
   * If {@code includeAttachments} is {@code true}, the report along with all launch attachments will be packed into a
   * ZIP archive and streamed to the client. Otherwise, only the report will be streamed in its native format (PDF, XLS,
   * or HTML).
   * </p>
   *
   * @param launchId           ID of the launch to export.
   * @param reportFormat       Format of the report to export. Supported values: "pdf", "xls", "html".
   * @param includeAttachments Whether to include all attachments related to the launch in a ZIP archive.
   * @param response           {@link HttpServletResponse} used to write the report (or archive) to the output stream.
   * @param user               Authenticated user requesting the export.
   * @throws ReportPortalException if the report or archive could not be written to the output stream.
   */
  void exportLaunch(Long launchId, String reportFormat, boolean includeAttachments, HttpServletResponse response,
      ReportPortalUser user, MembershipDetails membershipDetails);

  /**
   * Get latest launches
   *
   * @param membershipDetails Membership details
   * @param filter         Filter data
   * @param pageable       Page details
   * @return Response Data
   */
  Page<LaunchResource> getLatestLaunches(MembershipDetails membershipDetails,
      Filter filter, Pageable pageable);

  /**
   * Get Launch resource by specified UUID
   *
   * @param launchId       Launch uuid
   * @param membershipDetails Membership details
   * @param pageable       Pagination information for the results
   * @return {@link ClusterInfoResource}
   */
  Page<ClusterInfoResource> getClusters(String launchId,
      MembershipDetails membershipDetails, Pageable pageable);

  boolean hasItemsWithIssues(Launch launch);
}
