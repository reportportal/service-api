/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.filter;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.ws.model.filter.UserFilterResource;
import org.springframework.data.domain.Pageable;

public interface ShareUserFilterHandler {

    /**
     * Share user filter to project
     *
     * @param projectName
     * @param filterId
     * @return list of all shared filters for the project
     */
    void shareFilter(String projectName, Long filterId);

    /**
     * Get all shared filters for specified project and user
     *
     * @param pageable       Page request
     * @param filter         Filter representation
     * @param projectName    Project Name
     * @param user           Report Portal User
     * @return {@link Iterable}
     */
    Iterable<UserFilterResource> getAllFilters(String projectName, Pageable pageable,
        Filter filter, ReportPortalUser user);

    /**
     * Get all shared filters  for specified project and user without own filters
     *
     * @param projectName    Project Name
     * @param pageable       Page request
     * @param filter         Filter representation
     * @param user           Report Portal User
     * @return {@link Iterable}
     */
    Iterable<UserFilterResource> getSharedFilters(String projectName, Pageable pageable,
        Filter filter, ReportPortalUser user);


}
