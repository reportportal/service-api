/*
 * Copyright 2025 EPAM Systems
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

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.ta.reportportal.entity.user.User;

/**
 * Service interface for managing user invitations in the system. Provides functionality for creating and processing user invitations.
 *
 * @author <a href="mailto:Siarhei_Hrabko@epam.com">Siarhei Hrabko</a>
 */
public interface UserInvitationService {

  /**
   * Assigns user to organizations and projects specified in the invitation request.
   *
   * @param invitationRq the invitation request containing organization and project assignments
   * @param user         the user to be assigned
   * @return the invitation with updated status and user information
   */
  Invitation assignUser(InvitationRequest invitationRq, User user);

  /**
   * Creates and sends an invitation to the specified email address.
   *
   * @param request the invitation request containing email and organization details
   * @return the created invitation with a generated link and status
   */
  Invitation sendInvitation(InvitationRequest request);
}
