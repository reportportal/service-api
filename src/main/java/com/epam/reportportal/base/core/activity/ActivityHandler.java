
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

package com.epam.reportportal.base.core.activity;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.model.ActivityResource;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.ActivityEventResource;
import com.epam.reportportal.base.model.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Ihar Kahadouski
 */
public interface ActivityHandler {

  /**
   * Load list of {@link ActivityResource} for specified {@link TestItem}
   *
   * @param membershipDetails Membership details {@link MembershipDetails}
   * @param filter            Filter
   * @param pageable          Page Details
   * @param predefinedFilter  Additional filter
   * @return Found activities
   */
  Iterable<ActivityResource> getActivitiesHistory(MembershipDetails membershipDetails,
      Filter filter, Queryable predefinedFilter, Pageable pageable);

  /**
   * Load {@link ActivityResource}
   *
   * @param membershipDetails Membership details {@link MembershipDetails}
   * @param activityId        ID of activity
   * @return Found Activity or NOT FOUND exception
   */
  ActivityResource getActivity(MembershipDetails membershipDetails, Long activityId);

  /**
   * Load list of {@link ActivityEventResource} for specified {@link TestItem}
   *
   * @param membershipDetails Membership details {@link MembershipDetails}
   * @param itemId            ID of test item
   * @param filter            Filter
   * @param pageable          Page Details
   * @return Found activities
   */
  Page<ActivityEventResource> getItemActivities(MembershipDetails membershipDetails,
      Long itemId, Filter filter, Pageable pageable);

  /**
   * Load list of {@link ActivityResource} for specified {@link Project}
   *
   * @param membershipDetails Membership details
   * @param filter            Filter
   * @param pageable          Page Details
   * @return Found activities
   */
  Iterable<ActivityResource> getItemActivities(MembershipDetails membershipDetails,
      Filter filter, Pageable pageable);
}
