/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ADMIN_ONLY;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_USER;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.composeBaseUrl;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.CompositeFilter;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.Queryable;
import com.epam.ta.reportportal.core.jasper.GetJasperReportHandler;
import com.epam.ta.reportportal.core.user.ApiKeyHandler;
import com.epam.ta.reportportal.core.user.CreateUserHandler;
import com.epam.ta.reportportal.core.user.DeleteUserHandler;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.core.user.GetUserHandler;
import com.epam.ta.reportportal.entity.jasper.ReportFormat;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
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
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.ResponseView;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.jooq.Operator;
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
@RequestMapping("/users")
@Tag(name = "user-controller", description = "User Controller")
public class UserController {

  private final CreateUserHandler createUserMessageHandler;

  private final EditUserHandler editUserMessageHandler;

  private final DeleteUserHandler deleteUserHandler;

  private final ApiKeyHandler apiKeyHandler;

  private final GetUserHandler getUserHandler;

  private final GetJasperReportHandler<User> jasperReportHandler;

  @Autowired
  public UserController(CreateUserHandler createUserMessageHandler,
      EditUserHandler editUserMessageHandler, DeleteUserHandler deleteUserHandler,
      GetUserHandler getUserHandler,
      @Qualifier("userJasperReportHandler") GetJasperReportHandler<User> jasperReportHandler,
      ApiKeyHandler apiKeyHandler) {
    this.createUserMessageHandler = createUserMessageHandler;
    this.editUserMessageHandler = editUserMessageHandler;
    this.deleteUserHandler = deleteUserHandler;
    this.getUserHandler = getUserHandler;
    this.jasperReportHandler = jasperReportHandler;
    this.apiKeyHandler = apiKeyHandler;
  }

  @PostMapping
  @ResponseStatus(CREATED)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Create specified user", description = "Allowable only for users with administrator role")
  public CreateUserRS createUserByAdmin(@RequestBody @Validated CreateUserRQFull rq,
      @AuthenticationPrincipal ReportPortalUser currentUser, HttpServletRequest request) {
    return createUserMessageHandler.createUserByAdmin(rq, currentUser, composeBaseUrl(request));
  }

  @Transactional
  @PostMapping(value = "/bid")
  @ResponseStatus(CREATED)
  @PreAuthorize("(hasPermission(#createUserRQ.getDefaultProject(), 'projectManagerPermission')) || hasRole('ADMINISTRATOR')")
  @Operation(summary = "Register invitation for user who will be created")
  public CreateUserBidRS createUserBid(@RequestBody @Validated CreateUserRQ createUserRQ,
      @AuthenticationPrincipal ReportPortalUser currentUser, HttpServletRequest request) {
    return createUserMessageHandler.createUserBid(createUserRQ, currentUser,
        composeBaseUrl(request)
    );
  }

  @PostMapping(value = "/registration")
  @ResponseStatus(CREATED)
  @Operation(summary = "Activate invitation and create user in system")
  public CreateUserRS createUser(@RequestBody @Validated CreateUserRQConfirm request,
      @RequestParam(value = "uuid") String uuid) {
    return createUserMessageHandler.createUser(request, uuid);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/registration")
  public UserBidRS getUserBidInfo(@RequestParam(value = "uuid") String uuid) {
    return getUserHandler.getBidInformation(uuid);
  }

  @DeleteMapping(value = "/{id}")
  @Operation(summary =  "Delete specified user")
  public OperationCompletionRS deleteUser(@PathVariable(value = "id") Long userId,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return deleteUserHandler.deleteUser(userId, currentUser);
  }

  @DeleteMapping
  @PreAuthorize(ADMIN_ONLY)
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified users by ids")
  public DeleteBulkRS deleteUsers(@RequestBody @Valid DeleteBulkRQ deleteBulkRQ,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteUserHandler.deleteUsers(deleteBulkRQ.getIds(), user);
  }

  @Transactional
  @PutMapping(value = "/{login}")
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary =  "Edit specified user", description = "Only for administrators and profile's owner")
  public OperationCompletionRS editUser(@PathVariable String login,
      @RequestBody @Validated EditUserRQ editUserRQ, @ActiveRole UserRole role,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return editUserMessageHandler.editUser(EntityUtils.normalizeId(login), editUserRQ, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{login}")
  @ResponseView(ModelViews.FullUserView.class)
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary =  "Return information about specified user", description = "Only for administrators and profile's owner")
  public UserResource getUser(@PathVariable String login,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return getUserHandler.getUser(EntityUtils.normalizeId(login), currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = { "", "/" })
  @Operation(summary = "Return information about current logged-in user")
  public UserResource getMyself(@AuthenticationPrincipal ReportPortalUser currentUser) {
    return getUserHandler.getUser(currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/all")
  @ResponseView(ModelViews.FullUserView.class)
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Return information about all users", description = "Allowable only for users with administrator role")
  public Iterable<UserResource> getUsers(@FilterFor(User.class) Filter filter,
      @SortFor(User.class) Pageable pageable, @FilterFor(User.class) Queryable queryable,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return getUserHandler.getAllUsers(new CompositeFilter(Operator.AND, filter, queryable),
        pageable
    );
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/registration/info")

  public YesNoRS validateInfo(@RequestParam(value = "username", required = false) String username,
      @RequestParam(value = "email", required = false) String email) {
    return getUserHandler.validateInfo(username, email);
  }

  @Transactional
  @PostMapping(value = "/password/restore")
  @ResponseStatus(OK)
  @Operation(summary = "Create a restore password request")
  public OperationCompletionRS restorePassword(@RequestBody @Validated RestorePasswordRQ rq,
      HttpServletRequest request) {
    return createUserMessageHandler.createRestorePasswordBid(rq, composeBaseUrl(request));
  }

  @Transactional
  @PostMapping(value = "/password/reset")
  @ResponseStatus(OK)
  @Operation(summary = "Reset password")
  public OperationCompletionRS resetPassword(@RequestBody @Validated ResetPasswordRQ rq) {
    return createUserMessageHandler.resetPassword(rq);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/password/reset/{uuid}")
  @ResponseStatus(OK)
  @Operation(summary = "Check if a restore password bid exists")
  public YesNoRS isRestorePasswordBidExist(@PathVariable String uuid) {
    return createUserMessageHandler.isResetPasswordBidExist(uuid);
  }

  @Transactional
  @PostMapping(value = "/password/change")
  @ResponseStatus(OK)
  @Operation(summary = "Change own password")
  public OperationCompletionRS changePassword(
      @RequestBody @Validated ChangePasswordRQ changePasswordRQ,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return editUserMessageHandler.changePassword(currentUser, changePasswordRQ);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{userName}/projects")
  @ResponseStatus(OK)
  public Map<String, UserResource.AssignedProject> getUserProjects(@PathVariable String userName,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return getUserHandler.getUserProjects(userName);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/search")
  @ResponseStatus(OK)
  @PreAuthorize(ADMIN_ONLY)
  public Iterable<UserResource> findUsers(@RequestParam(value = "term") String term,
      Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
    return getUserHandler.searchUsers(term, pageable);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/export")
  @PreAuthorize(ADMIN_ONLY)
  @Operation(summary =  "Exports information about all users", description = "Allowable only for users with administrator role")
  public void export(@Parameter(schema = @Schema(allowableValues = "csv"))
  @RequestParam(value = "view", required = false, defaultValue = "csv") String view,
      @FilterFor(User.class) Filter filter, @FilterFor(User.class) Queryable queryable,
      @AuthenticationPrincipal ReportPortalUser currentUser, HttpServletResponse response) {

    ReportFormat format = jasperReportHandler.getReportFormat(view);
    response.setContentType(format.getContentType());

    response.setHeader(com.google.common.net.HttpHeaders.CONTENT_DISPOSITION,
        String.format("attachment; filename=\"RP_USERS_%s_Report.%s\"", format.name(),
            format.getValue()
        )
    );

    try (OutputStream outputStream = response.getOutputStream()) {
      getUserHandler.exportUsers(format, outputStream,
          new CompositeFilter(Operator.AND, filter, queryable)
      );
    } catch (IOException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Unable to write data to the response."
      );
    }
  }

  @PostMapping(value = "/{userId}/api-keys")
  @ResponseStatus(CREATED)
  @Operation(summary = "Create new Api Key for current user")
  public ApiKeyRS createApiKey(@RequestBody @Validated ApiKeyRQ apiKeyRQ,
      @AuthenticationPrincipal ReportPortalUser currentUser, @PathVariable Long userId) {
    return apiKeyHandler.createApiKey(apiKeyRQ.getName(), currentUser.getUserId());
  }

  @DeleteMapping(value = "/{userId}/api-keys/{keyId}")
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified Api Key")
  public OperationCompletionRS deleteApiKey(@PathVariable Long keyId, @PathVariable Long userId) {
    return apiKeyHandler.deleteApiKey(keyId);
  }

  @GetMapping(value = "/{userId}/api-keys")
  @ResponseStatus(OK)
  @Operation(summary = "Get List of users Api Keys")
  public ApiKeysRS getUsersApiKeys(@AuthenticationPrincipal ReportPortalUser currentUser,
      @PathVariable Long userId) {
    return apiKeyHandler.getAllUsersApiKeys(currentUser.getUserId());
  }
}
