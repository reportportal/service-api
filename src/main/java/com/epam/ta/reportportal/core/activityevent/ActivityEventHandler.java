/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.ta.reportportal.core.activityevent;

import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.ActivityEventResource;
import com.epam.ta.reportportal.model.PagedResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Activity Event Handler.
 *
 * @author Ryhor_Kukharenka
 */
public interface ActivityEventHandler {

  /**
   * Get ActivityEvent page representation.
   *
   * @param filter   Filter
   * @param pageable Page Details
   * @return Find activity events in page view
   */
  PagedResponse<ActivityEventResource> getActivityEventsHistory(Queryable filter,
      Pageable pageable);

  /**
   * Get list of specified subjectName in project activities.
   *
   * @param membershipDetails Membership details
   * @param value          Filter value
   * @return List of found user logins
   */
  List<String> getSubjectNames(MembershipDetails membershipDetails, String value);

}
