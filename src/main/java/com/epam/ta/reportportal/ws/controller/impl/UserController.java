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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.user.ICreateUserHandler;
import com.epam.ta.reportportal.core.user.IDeleteUserHandler;
import com.epam.ta.reportportal.core.user.IEditUserHandler;
import com.epam.ta.reportportal.core.user.IGetUserHandler;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.controller.IUserController;
import com.epam.ta.reportportal.ws.model.ModelViews;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.*;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.ResponseView;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.util.Map;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_USER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Controller implementation for operations with users
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Controller
@RequestMapping("/user")
public class UserController implements IUserController {

	@Autowired
	private ICreateUserHandler createUserMessageHandler;

	@Autowired
	private IEditUserHandler editUserMessageHandler;

	@Autowired
	private IDeleteUserHandler deleteUserMessageHandler;

	@Autowired
	private IGetUserHandler getUserHandler;

	@Override
	@RequestMapping(method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Create specified user", notes = "Allowable only for users with administrator role")
	public CreateUserRS createUserByAdmin(@RequestBody @Validated CreateUserRQFull rq, Principal principal, HttpServletRequest request) {
		String basicURL = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
				.replacePath(null)
				.replaceQuery(null)
				.build()
				.toUriString();
		return createUserMessageHandler.createUserByAdmin(rq, principal.getName(), basicURL);
	}

	@Override
	@RequestMapping(value = "/bid", method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@PreAuthorize("hasPermission(#createUserRQ.getDefaultProject(), 'projectManagerPermission')")
	@ApiOperation("Register invitation for user who will be created")
	public CreateUserBidRS createUserBid(@RequestBody @Validated CreateUserRQ createUserRQ, Principal principal,
			HttpServletRequest request) {
		/*
		 * Use Uri components since they are aware of x-forwarded-host headers
		 */
		URI rqUrl = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
				.replacePath(null)
				.replaceQuery(null)
				.build()
				.toUri();
		return createUserMessageHandler.createUserBid(createUserRQ, principal, rqUrl.toASCIIString());
	}

	@Override
	@RequestMapping(value = "/registration", method = POST)
	@ResponseStatus(CREATED)
	@ResponseBody
	@ApiOperation("Activate invitation and create user in system")
	public CreateUserRS createUser(@RequestBody @Validated CreateUserRQConfirm request, @RequestParam(value = "uuid") String uuid,
			Principal principal) {
		return createUserMessageHandler.createUser(request, uuid, principal);
	}

	@Override
	@RequestMapping(value = "/registration", method = GET)
	@ResponseBody
	@ApiIgnore
	public UserBidRS getUserBidInfo(@RequestParam(value = "uuid") String uuid) {
		return getUserHandler.getBidInformation(uuid);
	}

	@Override
	@RequestMapping(value = "/{login}", method = DELETE)
	@ResponseBody
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete specified user", notes = "Allowable only for users with administrator role")
	public OperationCompletionRS deleteUser(@PathVariable String login, Principal principal) {
		return deleteUserMessageHandler.deleteUser(EntityUtils.normalizeId(login), principal.getName());
	}

	@Override
	@RequestMapping(value = "/{login}", method = PUT)
	@ResponseBody
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Edit specified user", notes = "Only for administrators and profile's owner")
	public OperationCompletionRS editUser(@PathVariable String login, @RequestBody @Validated EditUserRQ editUserRQ,
			@ActiveRole UserRole role, Principal principal) {
		return editUserMessageHandler.editUser(EntityUtils.normalizeId(login), editUserRQ, role);
	}

	@Override
	@RequestMapping(value = "/{login}", method = GET)
	@ResponseBody
	@ResponseView(ModelViews.FullUserView.class)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Return information about specified user", notes = "Only for administrators and profile's owner")
	public UserResource getUser(@PathVariable String login, Principal principal) {
		return getUserHandler.getUser(EntityUtils.normalizeId(login), principal);
	}

	@Override
	@RequestMapping(value = { "", "/" }, method = GET)
	@ResponseBody
	@ResponseView(ModelViews.FullUserView.class)
	@ApiOperation("Return information about current logged-in user")
	public UserResource getMyself(Principal principal) {
		return getUserHandler.getUser(EntityUtils.normalizeId(principal.getName()), principal);
	}

	@Override
	@RequestMapping(value = "/all", method = GET)
	@ResponseBody
	@ResponseView(ModelViews.FullUserView.class)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Return information about all users", notes = "Allowable only for users with administrator role")
	public Iterable<UserResource> getUsers(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			Principal principal) {
		return getUserHandler.getAllUsers(filter, pageable);
	}

	@Override
	@RequestMapping(value = "/registration/info", method = GET)
	@ResponseBody
	@ApiIgnore
	public YesNoRS validateInfo(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "email", required = false) String email) {
		return getUserHandler.validateInfo(username, email);
	}

	@Override
	@RequestMapping(value = "/password/restore", method = POST)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Create a restore password request")
	public OperationCompletionRS restorePassword(@RequestBody @Validated RestorePasswordRQ rq, HttpServletRequest request) {
		/*
		 * Use Uri components since they are aware of x-forwarded-host headers
		 */
		String baseUrl = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
				.replacePath(null)
				.replaceQuery(null)
				.build()
				.toUriString();
		return createUserMessageHandler.createRestorePasswordBid(rq, baseUrl);
	}

	@Override
	@RequestMapping(value = "/password/reset", method = POST)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Reset password")
	public OperationCompletionRS resetPassword(@RequestBody @Validated ResetPasswordRQ rq) {
		return createUserMessageHandler.resetPassword(rq);
	}

	@Override
	@RequestMapping(value = "/password/reset/{id}", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Check if a restore password bid exists")
	public YesNoRS isRestorePasswordBidExist(@PathVariable String id) {
		return createUserMessageHandler.isResetPasswordBidExist(id);
	}

	@Override
	@RequestMapping(value = "/password/change", method = POST)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Change own password")
	public OperationCompletionRS changePassword(@RequestBody @Validated ChangePasswordRQ changePasswordRQ, Principal principal) {
		return editUserMessageHandler.changePassword(principal.getName(), changePasswordRQ);
	}

	@RequestMapping(value = "/{userName}/projects", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	public Map<String, UserResource.AssignedProject> getUserProjects(@PathVariable String userName, Principal principal) {
		return getUserHandler.getUserProjects(userName);
	}

	@Override
	@RequestMapping(value = "/search", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	@PreAuthorize(ADMIN_ONLY)
	public Iterable<UserResource> findUsers(@RequestParam(value = "term") String term, Pageable pageable, Principal principal) {
		return getUserHandler.searchUsers(term, pageable);
	}
}
