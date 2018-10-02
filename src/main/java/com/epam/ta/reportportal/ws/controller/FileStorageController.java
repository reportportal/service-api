/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * Controller for Internal File System
 *
 * @author Andrei Varabyeu
 */
public interface FileStorageController {

	/**
	 * Retrieves file from file system
	 *
	 * @param dataId
	 * @param httpServletResponse
	 * @param user
	 */
	void getFile(String dataId, HttpServletResponse httpServletResponse, ReportPortalUser user);

	/**
	 * Retrieves avatar for current login user
	 *
	 * @param user
	 * @param response
	 */
	void getMyPhoto(ReportPortalUser user, HttpServletResponse response);

	/**
	 * Get specified user photo as binary data from storage
	 *
	 * @param username
	 * @param response
	 * @param user
	 */
	void getUserPhoto(String username, HttpServletResponse response, ReportPortalUser user);

	/**
	 * Upload user's photo
	 *
	 * @param file
	 * @param user
	 * @return
	 */
	OperationCompletionRS uploadPhoto(MultipartFile file, ReportPortalUser user);

	/**
	 * Delete user's photo
	 *
	 * @param user
	 * @return
	 */
	OperationCompletionRS deletePhoto(ReportPortalUser user);

}