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

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * Controller for Internal File System
 *
 * @author Andrei Varabyeu
 */
public interface IFileStorageController {

	/**
	 * Retrieves file from file system
	 *
	 * @param projectName
	 * @param dataId
	 * @param httpServletResponse
	 * @param principal
	 */
	void getFile(String projectName, String dataId, HttpServletResponse httpServletResponse, Principal principal);

	/**
	 * Retrieves avatar for current login user
	 *
	 * @param principal
	 * @param response
	 */
	void getMyPhoto(Principal principal, HttpServletResponse response);

	/**
	 * Get specified user photo as binary data from storage
	 *
	 * @param username
	 * @param response
	 * @param principal
	 */
	void getUserPhoto(String username, HttpServletResponse response, Principal principal);

	/**
	 * Upload user's photo
	 *
	 * @param file
	 * @param principal
	 * @return
	 */
	OperationCompletionRS uploadPhoto(MultipartFile file, Principal principal);

	/**
	 * Delete user's photo
	 *
	 * @param principal
	 * @return
	 */
	OperationCompletionRS deletePhoto(Principal principal);

}