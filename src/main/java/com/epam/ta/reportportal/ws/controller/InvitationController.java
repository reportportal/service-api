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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.epam.reportportal.api.InvitationApi;
import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationActivation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.ta.reportportal.core.user.impl.UserInvitationHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing user invitations. This controller handles the creation, retrieval, and activation of user invitations. It provides
 * endpoints for sending invitations to users and activating them.
 *
 * @author <a href="mailto:Siarhei_Hrabko@epam.com">Siarhei Hrabko</a>
 */
@RestController
public class InvitationController extends BaseController implements InvitationApi {

  private final UserInvitationHandler userInvitationHandler;

  /**
   * Constructor for the InvitationController.
   *
   * @param userInvitationHandler The handler for user invitations.
   */
  public InvitationController(UserInvitationHandler userInvitationHandler) {
    this.userInvitationHandler = userInvitationHandler;
  }

  @Transactional
  @Override
  @PreAuthorize(INVITATION_ALLOWED)
  public ResponseEntity<Invitation> postInvitations(InvitationRequest invitationRequest) {
    return ResponseEntity
        .status(CREATED)
        .body(userInvitationHandler.createUserInvitation(invitationRequest));
  }


  @Override
  public ResponseEntity<Invitation> getInvitationsId(String invitationId) {
    return ResponseEntity
        .status(OK)
        .body(userInvitationHandler.getInvitation(invitationId));

  }

  @Override
  public ResponseEntity<Invitation> putInvitationsId(String invitationId,
      InvitationActivation invitationActivation) {
    return ResponseEntity
        .status(OK)
        .body(userInvitationHandler.activate(invitationActivation, invitationId));
  }

}
