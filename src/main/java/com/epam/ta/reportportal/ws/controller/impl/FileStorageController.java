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

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.user.impl.EditUserHandler;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.UserRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.controller.IFileStorageController;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

/**
 * Implementation of file storage controller. <br>
 * We do not use handlers there since there is no business logic and domain
 * model conversion
 *
 * @author Andrei Varabyeu
 */
@Controller
public class FileStorageController implements IFileStorageController {

	@Autowired
	private DataStorage binaryDataStorage;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EditUserHandler editUserHandler;

	/**
	 * (non-Javadoc)
	 *
	 * @see com.epam.ta.reportportal.ws.controller.IFileStorageController#getFile(String, String, HttpServletResponse, Principal)
	 */
	// TODO remove project from here
	@RequestMapping(value = "/{projectName}/data/{dataId}", method = RequestMethod.GET)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@Override
	public void getFile(@PathVariable String projectName, @PathVariable String dataId, HttpServletResponse response, Principal principal) {
		toResponse(response, binaryDataStorage.fetchData(dataId));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.epam.ta.reportportal.ws.controller.IFileStorageController#getMyPhoto
	 * (java.security.Principal, javax.servlet.http.HttpServletResponse)
	 */
	@RequestMapping(value = "/data/photo", method = RequestMethod.GET)
	@Override
	@ApiOperation("Get photo of current user")
	public void getMyPhoto(Principal principal, HttpServletResponse response) {
		toResponse(response, userRepository.findUserPhoto(principal.getName()));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.epam.ta.reportportal.ws.controller.IFileStorageController#getMyPhoto
	 * (java.security.Principal, javax.servlet.http.HttpServletResponse)
	 */
	@RequestMapping(value = "/data/userphoto", method = RequestMethod.GET)
	@Override
	@ApiOperation("Get user's photo")
	public void getUserPhoto(@RequestParam(value = "id") String username, HttpServletResponse response, Principal principal) {
		toResponse(response, userRepository.findUserPhoto(EntityUtils.normalizeId(username)));
	}

	@Override
	@RequestMapping(value = "/data/photo", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation("Upload user's photo")
	public OperationCompletionRS uploadPhoto(@RequestParam("file") MultipartFile file, Principal principal) {
		return editUserHandler.uploadPhoto(principal.getName(), file);
	}

	@Override
	@RequestMapping(value = "/data/photo", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation("Delete user's photo")
	public OperationCompletionRS deletePhoto(Principal principal) {
		return editUserHandler.deletePhoto(principal.getName());
	}

	/**
	 * Copies provided {@link BinaryData} to Response
	 *
	 * @param response   Response
	 * @param binaryData Binary data object
	 */
	private void toResponse(HttpServletResponse response, BinaryData binaryData) {
		if (binaryData != null) {
			response.setContentType(binaryData.getContentType());
			response.setContentLength(binaryData.getLength().intValue());

			try {
				IOUtils.copy(binaryData.getInputStream(), response.getOutputStream());
			} catch (IOException e) {
				throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
			}
		} else {
			response.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}

}
