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
import com.epam.ta.reportportal.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.model.user.EditUserRQ;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import org.springframework.web.multipart.MultipartFile;

/**
 * Edit request handler
 *
 * @author Aliaksandr_Kazantsau
 */
public interface EditUserHandler {

  /**
   * Edit User
   *
   * @param username   Name of user
   * @param editUserRQ Edit request
   * @param editor     User performing the edit operation
   * @return Completion result
   */
  OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, ReportPortalUser editor);

  /**
   * Upload photo
   *
   * @param username Name of user
   * @param file     New photo
   * @return Completion result
   */
  OperationCompletionRS uploadPhoto(String username, MultipartFile file);

  /**
   * Upload photo
   *
   * @param userId id of user
   * @param file   New photo
   * @return Completion result
   */
  OperationCompletionRS uploadPhoto(Long userId, MultipartFile file);

  /**
   * Delete user's photo
   *
   * @param username Name of user
   * @return Completion result
   */
  OperationCompletionRS deletePhoto(String username);

  /**
   * Change password
   *
   * @param currentUser      User performing the edit operation
   * @param changePasswordRQ Request body
   * @return Completion result
   */
  OperationCompletionRS changePassword(ReportPortalUser currentUser,
      ChangePasswordRQ changePasswordRQ);
}
