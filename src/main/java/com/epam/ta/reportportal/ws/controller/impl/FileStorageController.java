/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.ws.controller.impl;

import com.epam.ta.reportportal.BinaryData;
import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.service.DataStoreService;
import com.epam.ta.reportportal.ws.controller.FileStorageController;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static com.epam.ta.reportportal.auth.permissions.Permissions.ASSIGNED_TO_PROJECT;

/**
 * @author Dzianis_Shybeka
 */
@RestController
@RequestMapping("/data")
public class FileStorageControllerImpl implements FileStorageController {

	@Autowired
	private UserRepository userRepository;

	//	@Autowired
	//	private EditUserHandler editUserHandler;

	private final DataStoreService dataStoreService;

	@Autowired
	public FileStorageControllerImpl(DataStoreService dataStoreService) {
		this.dataStoreService = dataStoreService;
	}

	@Override
	@GetMapping(value = "/{dataId}")
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	public void getFile(@PathVariable("dataId") String dataId, HttpServletResponse response,
			@AuthenticationPrincipal ReportPortalUser user) {
		toResponse(response, dataStoreService.load(dataId));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.epam.ta.reportportal.ws.controller.FileStorageController#getMyPhoto
	 * (java.security.Principal, javax.servlet.http.HttpServletResponse)
	 */
	@RequestMapping(value = "/data/photo", method = RequestMethod.GET)
	@Override
	@ApiOperation("Get photo of current user")
	public void getMyPhoto(@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {
		toResponse(response, userRepository.findUserPhoto(user.getName()));
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see com.epam.ta.reportportal.ws.controller.FileStorageController#getMyPhoto
	 * (java.security.Principal, javax.servlet.http.HttpServletResponse)
	 */
	@RequestMapping(value = "/data/userphoto", method = RequestMethod.GET)
	@Override
	@ApiOperation("Get user's photo")
	public void getUserPhoto(@RequestParam(value = "id") String username, HttpServletResponse response,
			@AuthenticationPrincipal ReportPortalUser user) {
		toResponse(response, userRepository.findUserPhoto(EntityUtils.normalizeId(username)));
	}

	@Override
	@RequestMapping(value = "/data/photo", method = RequestMethod.POST)
	@ResponseBody
	@ApiOperation("Upload user's photo")
	public OperationCompletionRS uploadPhoto(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal ReportPortalUser user) {
		return editUserHandler.uploadPhoto(user.getName(), file);
	}

	@Override
	@RequestMapping(value = "/data/photo", method = RequestMethod.DELETE)
	@ResponseBody
	@ApiOperation("Delete user's photo")
	public OperationCompletionRS deletePhoto(@AuthenticationPrincipal ReportPortalUser user) {
		return editUserHandler.deletePhoto(user.getName());
	}

	/**
	 * Copies provided {@link BinaryData} to Response
	 *
	 * @param response    Response
	 * @param inputStream Stored data
	 */
	private void toResponse(HttpServletResponse response, InputStream inputStream) {
		if (inputStream != null) {

			try {
				IOUtils.copy(inputStream, response.getOutputStream());
			} catch (IOException e) {
				throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
			}
		} else {
			response.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}
}
