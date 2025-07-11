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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_EDIT_USER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_OWNER;
import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.composeBaseUrl;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
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
import com.epam.ta.reportportal.model.ApiKeyRQ;
import com.epam.ta.reportportal.model.ApiKeyRS;
import com.epam.ta.reportportal.model.ApiKeysRS;
import com.epam.ta.reportportal.model.DeleteBulkRS;
import com.epam.ta.reportportal.model.ModelViews;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.model.YesNoRS;
import com.epam.ta.reportportal.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.model.user.EditUserRQ;
import com.epam.ta.reportportal.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.model.user.RestorePasswordRQ;
import com.epam.ta.reportportal.model.user.UserResource;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.epam.ta.reportportal.ws.resolver.ActiveRole;
import com.epam.ta.reportportal.ws.resolver.FilterFor;
import com.epam.ta.reportportal.ws.resolver.ResponseView;
import com.epam.ta.reportportal.ws.resolver.SortFor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/v1/users")
@Tag(name = "Users", description = "User Controller")
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

  @DeleteMapping(value = "/{id}")
  @Operation(summary = "Delete specified user")
  public OperationCompletionRS deleteUser(@PathVariable(value = "id") Long userId,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return deleteUserHandler.deleteUser(userId, currentUser);
  }

  @DeleteMapping
  @PreAuthorize(IS_ADMIN)
  @ResponseStatus(OK)
  @Operation(summary = "Delete specified users by ids")
  public DeleteBulkRS deleteUsers(@RequestParam(value = "ids") List<Long> ids,
      @AuthenticationPrincipal ReportPortalUser user) {
    return deleteUserHandler.deleteUsers(ids, user);
  }

  @Transactional
  @PutMapping(value = "/{login}")
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary = "Edit specified user", description = "Only for administrators and profile's owner")
  public OperationCompletionRS editUser(@PathVariable String login,
      @RequestBody @Validated EditUserRQ editUserRQ, @ActiveRole UserRole role,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return editUserMessageHandler.editUser(EntityUtils.normalizeId(login), editUserRQ, currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/{login}")
  @ResponseView(ModelViews.FullUserView.class)
  @PreAuthorize(ALLOWED_TO_EDIT_USER)
  @Operation(summary = "Return information about specified user", description = "Only for administrators and profile's owner")
  public UserResource getUser(@PathVariable String login,
      @AuthenticationPrincipal ReportPortalUser currentUser) {
    return getUserHandler.getUser(EntityUtils.normalizeId(login), currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = {"", "/"})
  @Operation(summary = "Return information about current logged-in user")
  public UserResource getMyself(@AuthenticationPrincipal ReportPortalUser currentUser) {
    return getUserHandler.getUser(currentUser);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/all")
  @ResponseView(ModelViews.FullUserView.class)
  @PreAuthorize(IS_ADMIN)
  @Operation(summary = "Return information about all users", description = "Allowable only for users with administrator role")
  public Page<UserResource> getUsers(@FilterFor(User.class) Filter filter,
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
  @PreAuthorize(IS_ADMIN)
  public Iterable<UserResource> findUsers(@RequestParam(value = "term") String term,
      Pageable pageable, @AuthenticationPrincipal ReportPortalUser user) {
    return getUserHandler.searchUsers(term, pageable);
  }

  @Transactional(readOnly = true)
  @GetMapping(value = "/export")
  @PreAuthorize(IS_ADMIN)
  @Operation(summary = "Exports information about all users", description = "Allowable only for users with administrator role")
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
  @PreAuthorize(ALLOWED_TO_OWNER)
  public OperationCompletionRS deleteApiKey(@PathVariable Long keyId, @PathVariable Long userId) {
    return apiKeyHandler.deleteApiKey(keyId, userId);
  }

  @GetMapping(value = "/{userId}/api-keys")
  @ResponseStatus(OK)
  @Operation(summary = "Get List of users Api Keys")
  public ApiKeysRS getUsersApiKeys(@AuthenticationPrincipal ReportPortalUser currentUser,
      @PathVariable Long userId) {
    return apiKeyHandler.getAllUsersApiKeys(currentUser.getUserId());
  }

}
