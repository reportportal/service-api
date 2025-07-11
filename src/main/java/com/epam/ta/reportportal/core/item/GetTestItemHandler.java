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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.ItemAttribute;
import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.ws.reporting.StatisticsResource;
import com.epam.ta.reportportal.ws.reporting.TestItemResource;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.data.domain.Pageable;

/**
 * GET operations for {@link TestItem}
 *
 * @author Andrei Varabyeu
 * @author Aliaksei Makayed
 */
public interface GetTestItemHandler {

  /**
   * Get {@link TestItem} instance
   *
   * @param testItemId     {@link TestItem#uuid}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   * @return {@link TestItemResource}
   */
  TestItemResource getTestItem(String testItemId, MembershipDetails membershipDetails,
      ReportPortalUser user);

  /**
   * Gets {@link TestItem} instances
   *
   * @param filter         {@link Filter}
   * @param pageable       {@link Pageable}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   * @param launchId       Launch id
   * @param filterId       Filter id
   * @param isLatest       true if
   * @param launchesLimit  response limit
   * @return {@link Iterable} of the {@link TestItemResource}
   */
  Page<TestItemResource> getTestItems(Queryable filter, Pageable pageable,
      MembershipDetails membershipDetails, ReportPortalUser user,
      @Nullable Long launchId, @Nullable Long filterId, boolean isLatest, int launchesLimit);

  /**
   * Gets {@link TestItem} instances
   *
   * @param filter         {@link Filter}
   * @param pageable       {@link Pageable}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   * @return {@link Page} of the {@link TestItemResource}
   */
  Page<TestItemResource> getTestItemsByProvider(Queryable filter, Pageable pageable,
      MembershipDetails membershipDetails, ReportPortalUser user,
      Map<String, String> params);

  /**
   * Gets accumulated statistics of items by data provider
   *
   * @param filter         {@link Filter}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   * @return Accumulated statistics
   */
  StatisticsResource getStatisticsByProvider(Queryable filter,
      MembershipDetails membershipDetails, ReportPortalUser user,
      Map<String, String> providerParams);

  /**
   * Get tickets that contains a term as a part inside for specified launch
   *
   * @param launchId {@link com.epam.ta.reportportal.entity.launch.Launch#id}
   * @param term     part of {@link com.epam.ta.reportportal.entity.bts.Ticket#ticketId} to search
   * @return {@link List} of {@link com.epam.ta.reportportal.entity.bts.Ticket#ticketId}
   */
  List<String> getTicketIds(Long launchId, String term);

  /**
   * Get tickets that contains a term as a part inside for specified project
   *
   * @param membershipDetails {@link MembershipDetails}
   * @param term           part of {@link Ticket#getTicketId()} to search
   * @return {@link List} of {@link Ticket#getTicketId()}
   */
  List<String> getTicketIds(MembershipDetails membershipDetails, String term);

  /**
   * Get specified attribute keys of all test items and launches for project with provided id
   *
   * @param launchFilterId {@link UserFilter#getId()} fo the
   *                       {@link
   *                       com.epam.ta.reportportal.commons.querygen.FilterTarget#LAUNCH_TARGET}
   * @param isLatest       Flag defines whether all or latest launches launches will be included in
   *                       the query condition
   * @param launchesLimit  Launches limit
   * @param membershipDetails {@link MembershipDetails}
   * @param keyPart        Part of the {@link ItemAttribute#getKey()} to search
   * @return {@link List} of the {@link ItemAttribute#getKey()}
   */
  List<String> getAttributeKeys(Long launchFilterId, boolean isLatest, int launchesLimit,
      MembershipDetails membershipDetails, String keyPart);

  /**
   * Retrieves a list of unique attribute keys based on the provided project details, a substring of
   * the key ({@code keyPart}), and an optional launch ID.
   *
   * <p>If {@code launchId} is {@code null}, the search spans all launches in the project.
   *
   * @param membershipDetails details of the project; must not be {@code null}.
   * @param keyPart        substring used to filter keys; must not be {@code null} or empty.
   * @param launchId       optional launch ID to restrict the search to a specific launch. If
   *                       {@code null}, all launches within the project are considered.
   * @return a list of unique attribute keys matching the criteria, or an empty list if none are
   * found.
   */
  List<String> getUniqueAttributeKeys(MembershipDetails membershipDetails, String keyPart, Long launchId);

  /**
   * Retrieves a list of unique attribute values based on the provided project details, a substring
   * of the value ({@code valuePart}), and an optional launch ID.
   *
   * <p>If {@code launchId} is {@code null}, the search spans all launches in the project.
   *
   * @param membershipDetails details of the project; must not be {@code null}.
   * @param valuePart      substring used to filter values; must not be {@code null} or empty.
   * @param launchId       optional launch ID to restrict the search to a specific launch. If
   *                       {@code null}, all launches within the project are considered.
   * @return a list of unique attribute values matching the criteria, or an empty list if none are
   * found.
   */
  List<String> getUniqueAttributeValues(MembershipDetails membershipDetails, String key, String valuePart,
      Long launchId);

  /**
   * Get specified attribute values
   *
   * @param launchId {@link com.epam.ta.reportportal.entity.launch.Launch#id}
   * @param key      Attribute key to search
   * @param value    part of the {@link com.epam.ta.reportportal.entity.ItemAttribute#value} to
   *                 search
   * @return {@link List} of the {@link com.epam.ta.reportportal.entity.ItemAttribute#value}
   */
  List<String> getAttributeValues(Long launchId, String key, String value);

  /**
   * Get attributes keys of test items under launches with provided name under
   * {@link com.epam.ta.reportportal.entity.project.Project} specified by `projectDetails`
   *
   * @param membershipDetails {@link MembershipDetails}
   * @param launchName     {@link Launch#getName()}
   * @param keyPart        part of the {@link ItemAttribute#getKey()} to search
   * @return {@link List} of the {@link ItemAttribute#getKey()}
   */
  List<String> getAttributeKeys(MembershipDetails membershipDetails, String launchName,
      String keyPart);

  /**
   * Get attributes values of test items under launches with provided name under
   * {@link com.epam.ta.reportportal.entity.project.Project} specified by `projectDetails`
   *
   * @param membershipDetails {@link MembershipDetails}
   * @param launchName     {@link Launch#getName()}
   * @param key            {@link ItemAttribute#getKey()}
   * @param valuePart      part of the {@link ItemAttribute#getValue()} to search
   * @return {@link List} of the {@link ItemAttribute#getValue()}
   */
  List<String> getAttributeValues(MembershipDetails membershipDetails, String launchName,
      String key, String valuePart);

  /**
   * @param ids            array of the {@link com.epam.ta.reportportal.entity.launch.Launch#id}
   * @param membershipDetails {@link MembershipDetails}
   * @param user           {@link ReportPortalUser}
   * @return {@link List} of the {@link TestItemResource}
   */
  List<TestItemResource> getTestItems(Long[] ids, MembershipDetails membershipDetails,
      ReportPortalUser user);
}
