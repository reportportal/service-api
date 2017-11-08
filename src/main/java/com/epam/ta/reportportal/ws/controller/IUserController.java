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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.*;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Map;

/**
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
public interface IUserController {

	/**
	 * Create new user from scratch (without invitation)
	 *
	 * @param createUserRQ
	 * @param principal
	 * @return instance of {@link CreateUserRS}
	 */
	CreateUserRS createUserByAdmin(CreateUserRQFull createUserRQ, Principal principal, HttpServletRequest request);

	/**
	 * Create new User (invite user)
	 *
	 * @param createUserRQ
	 * @param principal
	 * @return instance of {@link OperationCompletionRS}
	 * @throws ReportPortalException
	 */
	CreateUserBidRS createUserBid(CreateUserRQ createUserRQ, Principal principal, HttpServletRequest request);

	/**
	 * Create new User (confirm user invitation)
	 *
	 * @param request
	 * @param uuid
	 * @param principal
	 * @return instance of {@link CreateUserRS}
	 * @throws ReportPortalException
	 */
	CreateUserRS createUser(CreateUserRQConfirm request, String uuid, Principal principal);

	/**
	 * Get user bid information
	 *
	 * @param uuid
	 * @return instance of {@link UserBidRS}
	 */
	UserBidRS getUserBidInfo(String uuid);

	/**
	 * Edit User by admin
	 *
	 * @param login
	 * @param editUserRQ
	 * @param userRole
	 * @param principal
	 * @return instance of {@link OperationCompletionRS}
	 * @throws ReportPortalException
	 */
	OperationCompletionRS editUser(String login, EditUserRQ editUserRQ, UserRole userRole, Principal principal);

	/**
	 * Delete User
	 *
	 * @param login
	 * @param principal
	 * @return instance of {@link OperationCompletionRS}
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteUser(String login, Principal principal);

	/**
	 * Get user by userId
	 *
	 * @param userId
	 * @param principal
	 * @return
	 */
	UserResource getUser(String userId, Principal principal);

	/**
	 * Get information about current logged-in user
	 *
	 * @param principal
	 * @return
	 */
	UserResource getMyself(Principal principal);

	/**
	 * Get all users list
	 *
	 * @param filter
	 * @param principal
	 * @return
	 */
	Iterable<UserResource> getUsers(Filter filter, Pageable pageable, Principal principal);

	/**
	 * Verify username or email existance
	 *
	 * @param username
	 * @param email
	 * @return
	 */
	YesNoRS validateInfo(String username, String email);

	/**
	 * Start reset password functionality (send reset password email)
	 *
	 * @param rq
	 * @param request
	 */
	OperationCompletionRS restorePassword(RestorePasswordRQ rq, HttpServletRequest request);

	/**
	 * Reset password
	 *
	 * @return
	 */
	OperationCompletionRS resetPassword(ResetPasswordRQ rq);

	/**
	 * Change own password
	 *
	 * @param changePasswordRQ
	 * @param principal
	 * @return
	 */
	OperationCompletionRS changePassword(ChangePasswordRQ changePasswordRQ, Principal principal);

	/**
	 * Verify restore password bid exist
	 *
	 * @param id
	 * @return
	 */
	YesNoRS isRestorePasswordBidExist(String id);

	Map<String, UserResource.AssignedProject> getUserProjects(String userName, Principal principal);

	Iterable<UserResource> findUsers(String phrase, Pageable pageable, Principal principal);
}