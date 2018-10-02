package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.core.user.DeleteUserHandler;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.UserConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
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

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private CreateUserHandler createUserMessageHandler;

	@Autowired
	private EditUserHandler editUserMessageHandler;

	@Autowired
	private DeleteUserHandler deleteUserMessageHandler;

	@Autowired
	private GetUserHandler getUserHandler;


	@RequestMapping(method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
//	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Create specified user", notes = "Allowable only for users with administrator role")
	public CreateUserRS createUserByAdmin(@RequestBody @Validated CreateUserRQFull rq, @AuthenticationPrincipal ReportPortalUser user, HttpServletRequest request) {
		String basicURL = UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request))
				.replacePath(null)
				.replaceQuery(null)
				.build()
				.toUriString();
		return createUserMessageHandler.createUserByAdmin(rq, user.getUsername(), basicURL);
	}


	@RequestMapping(value = "/bid", method = POST)
	@ResponseBody
	@ResponseStatus(CREATED)
	@PreAuthorize("hasPermission(#createUserRQ.getDefaultProject(), 'projectManagerPermission')")
	@ApiOperation("Register invitation for user who will be created")
	public CreateUserBidRS createUserBid(@RequestBody @Validated CreateUserRQ createUserRQ, @AuthenticationPrincipal ReportPortalUser user,
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


	@RequestMapping(value = "/registration", method = POST)
	@ResponseStatus(CREATED)
	@ResponseBody
	@ApiOperation("Activate invitation and create user in system")
	public CreateUserRS createUser(@RequestBody @Validated CreateUserRQConfirm request, @RequestParam(value = "uuid") String uuid,
			@AuthenticationPrincipal ReportPortalUser user) {
		return createUserMessageHandler.createUser(request, uuid, principal);
	}


	@RequestMapping(value = "/registration", method = GET)
	@ResponseBody
	@ApiIgnore
	public UserBidRS getUserBidInfo(@RequestParam(value = "uuid") String uuid) {
		return getUserHandler.getBidInformation(uuid);
	}


	@RequestMapping(value = "/{login}", method = DELETE)
	@ResponseBody
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Delete specified user", notes = "Allowable only for users with administrator role")
	public OperationCompletionRS deleteUser(@PathVariable String login, @AuthenticationPrincipal ReportPortalUser user) {
		return deleteUserMessageHandler.deleteUser(EntityUtils.normalizeId(login), principal.getName());
	}


	@RequestMapping(value = "/{login}", method = PUT)
	@ResponseBody
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Edit specified user", notes = "Only for administrators and profile's owner")
	public OperationCompletionRS editUser(@PathVariable String login, @RequestBody @Validated EditUserRQ editUserRQ,
			@ActiveRole UserRole role, @AuthenticationPrincipal ReportPortalUser user) {
		return editUserMessageHandler.editUser(EntityUtils.normalizeId(login), editUserRQ, role);
	}


	@RequestMapping(value = "/{login}", method = GET)
	@ResponseBody
	@ResponseView(ModelViews.FullUserView.class)
	@PreAuthorize(ALLOWED_TO_EDIT_USER)
	@ApiOperation(value = "Return information about specified user", notes = "Only for administrators and profile's owner")
	public UserResource getUser(@PathVariable String login, @AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.getUser(EntityUtils.normalizeId(login), principal);
	}


	@RequestMapping(value = { "", "/" }, method = GET)
	@ResponseBody
	@ResponseView(ModelViews.FullUserView.class)
	@ApiOperation("Return information about current logged-in user")
	public UserResource getMyself(@AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.getUser(EntityUtils.normalizeId(principal.getName()), principal);
	}


	@RequestMapping(value = "/all", method = GET)
	@ResponseBody
	@ResponseView(ModelViews.FullUserView.class)
	@PreAuthorize(ADMIN_ONLY)
	@ApiOperation(value = "Return information about all users", notes = "Allowable only for users with administrator role")
	public Iterable<UserResource> getUsers(@FilterFor(User.class) Filter filter, @SortFor(User.class) Pageable pageable,
			@AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.getAllUsers(filter, pageable);
	}


	@RequestMapping(value = "/registration/info", method = GET)
	@ResponseBody
	@ApiIgnore
	public YesNoRS validateInfo(@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "email", required = false) String email) {
		return getUserHandler.validateInfo(username, email);
	}


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


	@RequestMapping(value = "/password/reset", method = POST)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Reset password")
	public OperationCompletionRS resetPassword(@RequestBody @Validated ResetPasswordRQ rq) {
		return createUserMessageHandler.resetPassword(rq);
	}


	@RequestMapping(value = "/password/reset/{id}", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiOperation("Check if a restore password bid exists")
	public YesNoRS isRestorePasswordBidExist(@PathVariable String id) {
		return createUserMessageHandler.isResetPasswordBidExist(id);
	}


	@RequestMapping(value = "/password/change", method = POST)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Change own password")
	public OperationCompletionRS changePassword(@RequestBody @Validated ChangePasswordRQ changePasswordRQ, @AuthenticationPrincipal ReportPortalUser user) {
		return editUserMessageHandler.changePassword(principal.getName(), changePasswordRQ);
	}

	@RequestMapping(value = "/{userName}/projects", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	public Map<String, UserResource.AssignedProject> getUserProjects(@PathVariable String userName, @AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.getUserProjects(userName);
	}


	@RequestMapping(value = "/search", method = GET)
	@ResponseStatus(OK)
	@ResponseBody
	@ApiIgnore
	@PreAuthorize(ADMIN_ONLY)
	public Iterable<UserResource> findUsers(@RequestParam(value = "term") String term, Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
		return getUserHandler.searchUsers(term, pageable);
	}

	@Transactional(readOnly = true)
	@GetMapping(value = { "", "/" })
	@ApiOperation("Return information about current logged-in user")
	public UserResource getMyself(@AuthenticationPrincipal ReportPortalUser user) {
		return UserConverter.TO_RESOURCE.apply(userRepository.findByLogin(EntityUtils.normalizeId(user.getUsername()))
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, user.getUsername())));
	}

}
