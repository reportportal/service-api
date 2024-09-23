/*
 * Copyright 2024 EPAM Systems
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

import static com.epam.ta.reportportal.auth.permissions.Permissions.INVITATION_ALLOWED;
import static com.epam.ta.reportportal.core.launch.util.LinkGenerator.composeBaseUrl;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.api.InvitationApi;
import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.ta.reportportal.core.user.UserInvitationHandler;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvitationController extends BaseController implements InvitationApi {

  private final UserInvitationHandler userInvitationHandler;
  private final HttpServletRequest httpServletRequest;

  public InvitationController(UserInvitationHandler userInvitationHandler,
      HttpServletRequest httpServletRequest) {
    this.userInvitationHandler = userInvitationHandler;
    this.httpServletRequest = httpServletRequest;
  }

  @Transactional
  @Override
  @PreAuthorize(INVITATION_ALLOWED)
  public ResponseEntity<Invitation> postInvitations(InvitationRequest invitationRequest) {
    var rpUser = getLoggedUser();

    // TODO: remove invitationRequest.getOrganizations duplicates?

    var response = userInvitationHandler.createUserInvitation(invitationRequest, rpUser,
        composeBaseUrl(httpServletRequest));

    return ResponseEntity
        .status(OK)
        .body(response);
  }

}
