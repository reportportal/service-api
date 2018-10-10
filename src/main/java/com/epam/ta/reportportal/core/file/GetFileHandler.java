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
package com.epam.ta.reportportal.core.file;

import com.epam.ta.reportportal.auth.ReportPortalUser;

import java.io.InputStream;

/**
 * @author Ivan Budaev
 */
public interface GetFileHandler {

	/**
	 * Returns {@link InputStream} for current logged-in user photo
	 *
	 * @param loggedInUser Logged-in {@link ReportPortalUser}
	 * @return {@link InputStream}
	 */
	InputStream getUserPhoto(ReportPortalUser loggedInUser);

	/**
	 * Returns {@link InputStream} for photo of the {@link com.epam.ta.reportportal.entity.user.User} with specified username
	 *
	 * @param username     Username of user which photo to get
	 * @param loggedInUser Logged-in {@link ReportPortalUser}
	 * @return {@link InputStream}
	 */
	InputStream getUserPhoto(String username, ReportPortalUser loggedInUser);

	/**
	 * Returns {@link InputStream} for the file with the specified id
	 *
	 * @param fileId Id of the file to get
	 * @return {@link InputStream}
	 */
	InputStream loadFileById(String fileId);
}
