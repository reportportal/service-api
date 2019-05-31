/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dzianis_Shybeka
 */
@RestController
@RequestMapping("/data")
public class FileStorageController {

	public static final String MAX_AGE_PREFIX = "private, max-age=";

	@Value("${rp.header.cache.photo}")
	private String photoCacheAge;

	private final EditUserHandler editUserHandler;

	private final GetFileHandler getFileHandler;

	@Autowired
	public FileStorageController(EditUserHandler editUserHandler, GetFileHandler getFileHandler) {
		this.editUserHandler = editUserHandler;
		this.getFileHandler = getFileHandler;
	}

	@Transactional(readOnly = true)
	@GetMapping(value = "/{dataId}")
	public void getFile(@PathVariable("dataId") String dataId, HttpServletResponse response,
			@AuthenticationPrincipal ReportPortalUser user) {
		toResponse(response, getFileHandler.loadFileById(dataId));
	}

	/**
	 * (non-Javadoc)
	 */
	@Transactional(readOnly = true)
	@GetMapping(value = "/photo")
	@ApiOperation("Get photo of current user")
	public void getMyPhoto(@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, MAX_AGE_PREFIX + photoCacheAge);
		toResponse(response, getFileHandler.getUserPhoto(user));
	}

	/**
	 * (non-Javadoc)
	 */
	@Transactional(readOnly = true)
	@GetMapping(value = "/userphoto")
	@ApiOperation("Get user's photo")
	public void getUserPhoto(@RequestParam(value = "id") String username, HttpServletResponse response,
			@AuthenticationPrincipal ReportPortalUser user) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, MAX_AGE_PREFIX + photoCacheAge);
		toResponse(response, getFileHandler.getUserPhoto(EntityUtils.normalizeId(username), user));
	}

	@Transactional
	@PostMapping(value = "/photo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ApiOperation("Upload user's photo")
	public OperationCompletionRS uploadPhoto(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal ReportPortalUser user) {
		return editUserHandler.uploadPhoto(EntityUtils.normalizeId(user.getUsername()), file);
	}

	@Transactional
	@DeleteMapping(value = "/photo")
	@ApiOperation("Delete user's photo")
	public OperationCompletionRS deletePhoto(@AuthenticationPrincipal ReportPortalUser user) {
		return editUserHandler.deletePhoto(EntityUtils.normalizeId(user.getUsername()));
	}

	/**
	 * Copies data from provided {@link InputStream} to Response
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
