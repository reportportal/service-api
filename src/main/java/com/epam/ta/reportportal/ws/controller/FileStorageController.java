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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.file.DeleteFilesHandler;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

import static com.epam.ta.reportportal.auth.permissions.Permissions.*;

/**
 * @author Dzianis_Shybeka
 */
@RestController
@RequestMapping("/v1/data")
public class FileStorageController {

	private final ProjectExtractor projectExtractor;
	private final EditUserHandler editUserHandler;
	private final GetFileHandler getFileHandler;
	private final DeleteFilesHandler deleteFilesHandler;

	@Autowired
	public FileStorageController(ProjectExtractor projectExtractor, EditUserHandler editUserHandler, GetFileHandler getFileHandler, DeleteFilesHandler deleteFilesHandler) {
		this.projectExtractor = projectExtractor;
		this.editUserHandler = editUserHandler;
		this.getFileHandler = getFileHandler;
		this.deleteFilesHandler = deleteFilesHandler;
	}

	@Transactional(readOnly = true)
	@PreAuthorize(ASSIGNED_TO_PROJECT)
	@GetMapping(value = "/{projectName}/{dataId}")
	public void getFile(@PathVariable String projectName, @PathVariable("dataId") Long dataId, HttpServletResponse response,
			@AuthenticationPrincipal ReportPortalUser user) {
		toResponse(response, getFileHandler.loadFileById(dataId, projectExtractor.extractProjectDetails(user, projectName)));
	}

	/**
	 * (non-Javadoc)
	 */
	@Transactional(readOnly = true)
	@GetMapping(value = "/photo")
	@ApiOperation("Get photo of current user")
	public void getMyPhoto(@AuthenticationPrincipal ReportPortalUser user, HttpServletResponse response,
			@RequestParam(value = "loadThumbnail", required = false) boolean loadThumbnail) {
		toResponse(response, getFileHandler.getUserPhoto(user, loadThumbnail));
	}

	/**
	 * (non-Javadoc)
	 */
	@Transactional(readOnly = true)
	@PreAuthorize(NOT_CUSTOMER)
	@GetMapping(value = "/{projectName}/userphoto")
	@ApiOperation("Get user's photo")
	public void getUserPhoto(@PathVariable String projectName, @RequestParam(value = "id") String username,
			@RequestParam(value = "loadThumbnail", required = false) boolean loadThumbnail, HttpServletResponse response,
			@AuthenticationPrincipal ReportPortalUser user) {
		BinaryData userPhoto = getFileHandler.getUserPhoto(EntityUtils.normalizeId(username), user, projectName, loadThumbnail);
		toResponse(response, userPhoto);
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

	@Transactional
	@PreAuthorize(ADMIN_ONLY)
	@PostMapping(value = "/clean", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@ApiOperation("Remove attachments from file storage according to uploaded csv file")
	public OperationCompletionRS removeAttachmentsByCsv(@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal ReportPortalUser user) {
		return deleteFilesHandler.removeFilesByCsv(file);
	}

	/**
	 * Copies data from provided {@link InputStream} to Response
	 *
	 * @param response   Response
	 * @param binaryData Stored data
	 */
	private void toResponse(HttpServletResponse response, BinaryData binaryData) {
		//TODO investigate stream closing requirement
		if (binaryData.getInputStream() != null) {
			try {
				response.setContentType(binaryData.getContentType());
				IOUtils.copy(binaryData.getInputStream(), response.getOutputStream());
			} catch (IOException e) {
				throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
			}
		} else {
			response.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}
}
