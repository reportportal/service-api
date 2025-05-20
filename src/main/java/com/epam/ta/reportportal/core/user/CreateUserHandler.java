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

import com.epam.reportportal.api.model.InstanceUser;
import com.epam.reportportal.api.model.NewUserRequest;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.model.YesNoRS;
import com.epam.ta.reportportal.model.user.CreateUserRQFull;
import com.epam.ta.reportportal.model.user.CreateUserRS;
import com.epam.ta.reportportal.model.user.ResetPasswordRQ;
import com.epam.ta.reportportal.model.user.RestorePasswordRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;

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
   * Create user by administrator
   *
   * @param request  Create request
   * @param user     User that creates request
   * @param basicUrl App URL for user URL to be created
   * @return User
   */
  InstanceUser createUser(NewUserRequest request, ReportPortalUser user, String basicUrl);


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
   * @param rq request for reset password
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
