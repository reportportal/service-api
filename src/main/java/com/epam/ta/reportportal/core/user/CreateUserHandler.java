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

package com.epam.ta.reportportal.core.user;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.YesNoRS;
import com.epam.ta.reportportal.ws.model.user.CreateUserBidRS;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQ;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQConfirm;
import com.epam.ta.reportportal.ws.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.ws.model.user.CreateUserRS;
import com.epam.ta.reportportal.ws.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.ws.model.user.RestorePasswordRQ;

/**
 * Post request handler
 *
 * @author Andrei_Ramanchuk
 */
public interface CreateUserHandler {

  /**
   * Create completed user object by administrator
   *
   * @param request  Create request
   * @param user     User that creates request
   * @param basicUrl App URL for user URL to be created
   * @return Operation result
   */
  CreateUserRS createUserByAdmin(CreateUserRQFull request, ReportPortalUser user, String basicUrl);

  /**
   * Create new User (confirm invitation)
   *
   * @param request Create request
   * @param uuid    Create UUID
   * @return Operation result
   */
  CreateUserRS createUser(CreateUserRQConfirm request, String uuid);

  /**
   * Create user bid (send invitation)
   *
   * @param request  Create Request
   * @param username Username/User that creates the request
   * @return Operation result
   */
  CreateUserBidRS createUserBid(CreateUserRQ request, ReportPortalUser username, String userRegURL);

  /**
   * Create restore password bid
   *
   * @param rq      Restore RQ
   * @param baseUrl App Base URL for reset URL to be built
   * @return Operation result
   */
  OperationCompletionRS createRestorePasswordBid(RestorePasswordRQ rq, String baseUrl);

  /**
   * Reset password
   *
   * @param rq
   * @return Operation result
   */
  OperationCompletionRS resetPassword(ResetPasswordRQ rq);

  /**
   * Verify reset password bid exist
   *
   * @param uuid Reset Password UUID
   * @return {@link YesNoRS}
   */
  YesNoRS isResetPasswordBidExist(String uuid);
}
