/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.user;

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.UserBidRS;
import com.epam.ta.reportportal.ws.model.user.UserResource;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.Map;

/**
 * @author Andrei_Ramanchuk
 */
public interface IGetUserHandler {

	/**
	 * Get specified user info
	 *
	 * @param username  Username
	 * @param principal Logged-in user
	 * @return
	 */
	UserResource getUser(String username, Principal principal);

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
	 * Get all users
	 *
	 * @param filter
	 * @param projectName
	 * @return
	 */
	Iterable<UserResource> getUsers(Filter filter, Pageable pageable, String projectName);

	Map<String, UserResource.AssignedProject> getUserProjects(String userName);

	Iterable<UserResource> getAllUsers(Filter filter, Pageable pageable);

	Iterable<UserResource> searchUsers(String term, Pageable pageable);
}