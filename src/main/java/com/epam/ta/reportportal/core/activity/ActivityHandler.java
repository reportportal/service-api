
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

package com.epam.ta.reportportal.core.activity;

import com.epam.reportportal.model.ActivityResource;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.ActivityEventResource;
import org.springframework.data.domain.Pageable;

/**
 * @author Ihar Kahadouski
 */
public interface ActivityHandler {

  /**
   * Load list of {@link com.epam.reportportal.model.ActivityResource} for specified
   * {@link com.epam.ta.reportportal.entity.item.TestItem}
   *
   * @param membershipDetails Membership details
   *                         {@link MembershipDetails}
   * @param filter           Filter
   * @param pageable         Page Details
   * @param predefinedFilter Additional filter
   * @return Found activities
   */
  Iterable<ActivityResource> getActivitiesHistory(MembershipDetails membershipDetails,
      Filter filter, Queryable predefinedFilter, Pageable pageable);

  /**
   * Load {@link com.epam.reportportal.model.ActivityResource}
   *
   * @param membershipDetails Membership details
   *                       {@link MembershipDetails}
   * @param activityId     ID of activity
   * @return Found Activity or NOT FOUND exception
   */
  ActivityResource getActivity(MembershipDetails membershipDetails, Long activityId);

  /**
   * Load list of {@link ActivityEventResource} for specified
   * {@link com.epam.ta.reportportal.entity.item.TestItem}
   *
   * @param membershipDetails Membership details
   *                       {@link MembershipDetails}
   * @param itemId         ID of test item
   * @param filter         Filter
   * @param pageable       Page Details
   * @return Found activities
   */
  Iterable<ActivityEventResource> getItemActivities(MembershipDetails membershipDetails,
      Long itemId, Filter filter, Pageable pageable);

  /**
   * Load list of {@link ActivityResource} for specified
   * {@link com.epam.ta.reportportal.entity.project.Project}
   *
   * @param membershipDetails Membership details
   * @param filter         Filter
   * @param pageable       Page Details
   * @return Found activities
   */
  Iterable<ActivityResource> getItemActivities(MembershipDetails membershipDetails,
      Filter filter, Pageable pageable);
}
