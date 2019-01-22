/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.user;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.UserBidRS;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Andrei_Ramanchuk
 */
public interface GetUserHandler {

	/**
	 * Get specified user info
	 *
	 * @param username    Username
	 * @param currentUser Logged-in username
	 * @return
	 */
	UserResource getUser(String username, ReportPortalUser currentUser);

	/**
	 * Get logged-in user info
	 *
	 * @param currentUser Logged-in username
	 * @return
	 */
	UserResource getUser(ReportPortalUser currentUser);

	/**
	 * Get information about user registration bid
	 *
	 * @param uuid
	 * @return
	 */
	UserBidRS getBidInformation(String uuid);

	/**
	 * Validate existence of username or email
	 *
	 * @param username
	 * @param email
	 * @return
	 */
	YesNoRS validateInfo(String username, String email);

	/**
	 * Get all users by filter with paging
	 *
	 * @param filter         Filter
	 * @param pageable       Paging
	 * @param projectDetails Project details
	 * @return Page of users
	 */
	Iterable<UserResource> getUsers(Filter filter, Pageable pageable, ReportPortalUser.ProjectDetails projectDetails);

	Map<String, UserResource.AssignedProject> getUserProjects(String userName);

	/**
	 * Get page of users with filter
	 *
	 * @param filter   Filter
	 * @param pageable Paging
	 * @return Page of {@link UserResource}
	 */
	Iterable<UserResource> getAllUsers(Queryable filter, Pageable pageable);

	/**
	 * Export Users info according to the {@link ReportFormat} type
	 *
	 * @param reportFormat {@link ReportFormat}
	 * @param filter       {@link Filter}
	 * @param outputStream {@link HttpServletResponse#getOutputStream()}
	 * @param pageable     {@link Pageable}
	 */
	void exportUsers(ReportFormat reportFormat, OutputStream outputStream, Queryable filter, Pageable pageable);
}