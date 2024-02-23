package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_USER;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.user.ApiKeyHandler;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.core.user.DeleteUserHandler;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.model.ApiKeyRQ;
import com.epam.ta.reportportal.model.ApiKeyRS;
import com.epam.ta.reportportal.model.ApiKeysRS;
import com.epam.ta.reportportal.model.DeleteBulkRQ;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.model.ModelViews;
import com.epam.ta.reportportal.model.YesNoRS;
import com.epam.ta.reportportal.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.model.user.CreateUserBidRS;
import com.epam.ta.reportportal.model.user.CreateUserRQ;
import com.epam.ta.reportportal.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.model.user.CreateUserRS;
import com.epam.ta.reportportal.model.user.EditUserRQ;
import com.epam.ta.reportportal.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.model.user.RestorePasswordRQ;
import com.epam.ta.reportportal.model.user.UserBidRS;
import com.epam.ta.reportportal.model.user.UserResource;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.ResponseView;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@Deprecated
@Tag(name = "deprecated-user-controller", description = "Deprecated User Controller")
public class DeprecatedUserController extends UserController {

  @Autowired
  public DeprecatedUserController(CreateUserHandler createUserMessageHandler,
      EditUserHandler editUserMessageHandler, DeleteUserHandler deleteUserHandler,
      GetUserHandler getUserHandler,
      @Qualifier("userJasperReportHandler") GetJasperReportHandler<User> jasperReportHandler,
      ApiKeyHandler apiKeyHandler) {
    super(createUserMessageHandler, editUserMessageHandler, deleteUserHandler, getUserHandler,
        jasperReportHandler, apiKeyHandler
    );
  }

  @Override
  @PostMapping
  @ResponseStatus(CREATED)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Create specified user (DEPRECATED)", description = "Allowable only for users with administrator role")
  public CreateUserRS createUserByAdmin(@RequestBody @Validated CreateUserRQFull rq,
      @AuthenticationPrincipal ReportPortalUser currentUser, HttpServletRequest request) {
    return super.createUserByAdmin(rq, currentUser, request);
  }

  @Transactional
  @PostMapping(value = "/bid")
  @ResponseStatus(CREATED)
  @PreAuthorize("(hasPermission(#createUserRQ.getDefaultProject(), 'projectManagerPermission')) || hasRole('ADMINISTRATOR')")
  @Operation(summary = "Register invitation for user who will be created (DEPRECATED)")
  public CreateUserBidRS createUserBid(@RequestBody @Validated CreateUserRQ createUserRQ,
      @AuthenticationPrincipal ReportPortalUser currentUser, HttpServletRequest request) {
    return super.createUserBid(createUserRQ, currentUser, request);
  }

  @PostMapping(value = "/registration")
  @ResponseStatus(CREATED)
  @Operation(summary = "Activate invitation and create user in system (DEPRECATED)")
  public CreateUserRS createUser(@RequestBody @Validated CreateUserRQConfirm request,
      @RequestParam(value = "uuid") String uuid) {
    return super.createUser(request, uuid);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/registration")
  @Operation(summary =  "Get user's registration info (DEPRECATED)")
  public UserBidRS getUserBidInfo(@RequestParam(value = "uuid") String uuid) {
    return super.getUserBidInfo(uuid);
  }

  @DeleteMapping(value = "/{id}")
  @Operation(summary =  "Delete specified user (DEPRECATED)")
  public OperationCompletionRS deleteUser(@PathVariable(value = "id") Long userId,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.deleteUser(userId, currentUser);
  }

  @DeleteMapping
  @PreAuthorize(ADMIN_ONLY)
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified users by ids (DEPRECATED)")
  public DeleteBulkRS deleteUsers(@RequestBody @Valid DeleteBulkRQ deleteBulkRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return super.deleteUsers(deleteBulkRQ.getIds(), user);
  }

  @Transactional
  @PutMapping(value = "/{login}")
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary =  "Edit specified user (DEPRECATED)", description = "Only for administrators and profile's owner")
  public OperationCompletionRS editUser(@PathVariable String login,
      @RequestBody @Validated EditUserRQ editUserRQ, @ActiveRole UserRole role,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.editUser(login, editUserRQ, role, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{login}")
  @ResponseView(ModelViews.FullUserView.class)
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary =  "Return information about specified user (DEPRECATED)", description = "Only for administrators and profile's owner")
  public UserResource getUser(@PathVariable String login,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.getUser(login, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = { "", "/" })
  @Operation(summary = "Return information about current logged-in user (DEPRECATED)")
  public UserResource getMyself(@AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.getMyself(currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/all")
  @ResponseView(ModelViews.FullUserView.class)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Return information about all users (DEPRECATED)", description = "Allowable only for users with administrator role")
  public Iterable<UserResource> getUsers(@FilterFor(User.class) Filter filter,
      @SortFor(User.class) Pageable pageable, @FilterFor(User.class) Queryable queryable,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.getUsers(filter, pageable, queryable, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/registration/info")
  @Operation(summary =  "Validate registration information (DEPRECATED)")
  public YesNoRS validateInfo(@RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "email", required = false) String email) {
    return super.validateInfo(username, email);
  }

  @Transactional
  @PostMapping(value = "/password/restore")
  @ResponseStatus(OK)
  @Operation(summary = "Create a restore password request (DEPRECATED)")
  public OperationCompletionRS restorePassword(@RequestBody @Validated RestorePasswordRQ rq,
      HttpServletRequest request) {
    return super.restorePassword(rq, request);
  }

  @Transactional
  @PostMapping(value = "/password/reset")
  @ResponseStatus(OK)
  @Operation(summary = "Reset password (DEPRECATED")
  public OperationCompletionRS resetPassword(@RequestBody @Validated ResetPasswordRQ rq) {
    return super.resetPassword(rq);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/password/reset/{uuid}")
  @ResponseStatus(OK)
  @Operation(summary = "Check if a restore password bid exists (DEPRECATED)")
  public YesNoRS isRestorePasswordBidExist(@PathVariable String uuid) {
    return super.isRestorePasswordBidExist(uuid);
  }

  @Transactional
  @PostMapping(value = "/password/change")
  @ResponseStatus(OK)
  @Operation(summary = "Change own password (DEPRECATED)")
  public OperationCompletionRS changePassword(
      @RequestBody @Validated ChangePasswordRQ changePasswordRQ,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.changePassword(changePasswordRQ, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{userName}/projects")
  @ResponseStatus(OK)
  @Operation(summary =  "Get user's projects (DEPRECATED)")
  public Map<String, UserResource.AssignedProject> getUserProjects(@PathVariable String userName,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return super.getUserProjects(userName, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/search")
  @ResponseStatus(OK)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Find users by term (DEPRECATED)", description = "Only for administrators")
  public Iterable<UserResource> findUsers(@RequestParam(value = "term") String term,
      Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
    return super.findUsers(term, pageable, user);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/export")
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Exports information about all users (DEPRECATED)", description = "Allowable only for users with administrator role")
  public void export(@Parameter(schema = @Schema(allowableValues = "csv"))
  @RequestParam(value = "view", required = false, defaultValue = "csv") String view,
      @FilterFor(User.class) Filter filter, @FilterFor(User.class) Queryable queryable,
      @AuthenticationPrincipal ReportPortalUser currentUser, HttpServletResponse response) {
    super.export(view, filter, queryable, currentUser, response);
  }

  @PostMapping(value = "/{userId}/api-keys")
  @ResponseStatus(CREATED)
  @Operation(summary = "Create new Api Key for current user (DEPRECATED)")
  public ApiKeyRS createApiKey(@RequestBody @Validated ApiKeyRQ apiKeyRQ,
      @AuthenticationPrincipal ReportPortalUser currentUser, @PathVariable Long userId) {
    return super.createApiKey(apiKeyRQ, currentUser, userId);
  }

  @DeleteMapping(value = "/{userId}/api-keys/{keyId}")
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified Api Key (DEPRECATED)")
  public OperationCompletionRS deleteApiKey(@PathVariable Long keyId, @PathVariable Long userId) {
    return super.deleteApiKey(keyId, userId);
  }

  @GetMapping(value = "/{userId}/api-keys")
  @ResponseStatus(OK)
  @Operation(summary = "Get List of users Api Keys (DEPRECATED)")
  public ApiKeysRS getUsersApiKeys(@AuthenticationPrincipal ReportPortalUser currentUser,
      @PathVariable Long userId) {
    return super.getUsersApiKeys(currentUser, userId);
  }
}