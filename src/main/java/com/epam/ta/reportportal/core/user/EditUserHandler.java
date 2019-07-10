/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.user;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.user.ChangePasswordRQ;
import com.epam.ta.reportportal.ws.model.user.EditUserRQ;
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
	 * @param username
	 * @param editUserRQ
	 * @param editor
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS editUser(String username, EditUserRQ editUserRQ, ReportPortalUser editor);

	/**
	 * Upload photo
	 *
	 * @param username
	 * @param file
	 * @return
	 */
	OperationCompletionRS uploadPhoto(String username, MultipartFile file);

	/**
	 * Delete user's photo
	 *
	 * @param username
	 * @return
	 */
	OperationCompletionRS deletePhoto(String username);

	/**
	 * Change password
	 *
	 * @param currentUser
	 * @param changePasswordRQ
	 * @return
	 */
	OperationCompletionRS changePassword(ReportPortalUser currentUser, ChangePasswordRQ changePasswordRQ);
}