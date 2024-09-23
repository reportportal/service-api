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

package com.epam.ta.reportportal.core.user;

import com.epam.reportportal.api.model.Invitation;
import com.epam.reportportal.api.model.InvitationRequest;
import com.epam.ta.reportportal.commons.ReportPortalUser;


public interface UserInvitationHandler {

  /**
   * Create user bid (send invitation)
   *
   * @param request    Create Request
   * @param username   Username/User that creates the request
   * @param userRegURL User registration url
   * @return Operation result
   */
  Invitation createUserInvitation(InvitationRequest request, ReportPortalUser username,
      String userRegURL);

}
