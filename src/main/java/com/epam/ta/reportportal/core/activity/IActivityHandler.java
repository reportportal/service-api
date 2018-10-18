/*
 *
 *  * Copyright 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.core.activity;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import com.epam.ta.reportportal.ws.model.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Ihar Kahadouski
 */
public interface IActivityHandler {

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.entity.item.TestItem}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param filter         Filter
	 * @param pageable       Page Details
	 * @return Found activities
	 */
	Iterable<ActivityResource> getActivitiesHistory(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);

	/**
	 * Load {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param activityId     ID of activity
	 * @return Found Activity or NOT FOUND exception
	 */
	ActivityResource getActivity(ReportPortalUser.ProjectDetails projectDetails, Long activityId);

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.entity.item.TestItem}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param itemId         ID of test item
	 * @param filter         Filter
	 * @param pageable       Page Details
	 * @return Found activities
	 */
	Iterable<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Long itemId, Filter filter,
			Pageable pageable);

	/**
	 * Load list of {@link com.epam.ta.reportportal.ws.model.ActivityResource}
	 * for specified
	 * {@link com.epam.ta.reportportal.entity.project.Project}
	 *
	 * @param projectDetails Details of project {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param filter         Filter
	 * @param pageable       Page Details
	 * @return Found activities
	 */
	Page<ActivityResource> getItemActivities(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable);
}
