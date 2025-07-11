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

import static com.epam.ta.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.ta.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.reportportal.model.ValidationConstraints;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.file.DeleteFilesHandler;
import com.epam.ta.reportportal.core.file.GetFileHandler;
import com.epam.ta.reportportal.core.user.EditUserHandler;
import com.epam.ta.reportportal.entity.attachment.BinaryData;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import com.google.common.net.HttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Dzianis_Shybeka
 */
@RestController
@RequestMapping("/v1/data")
@Tag(name = "File Storage", description = "Files Storage API collection")
public class FileStorageController {

  private final ProjectExtractor projectExtractor;
  private final EditUserHandler editUserHandler;
  private final GetFileHandler getFileHandler;
  private final DeleteFilesHandler deleteFilesHandler;

  @Autowired
  public FileStorageController(ProjectExtractor projectExtractor, EditUserHandler editUserHandler,
      GetFileHandler getFileHandler, DeleteFilesHandler deleteFilesHandler) {
    this.projectExtractor = projectExtractor;
    this.editUserHandler = editUserHandler;
    this.getFileHandler = getFileHandler;
    this.deleteFilesHandler = deleteFilesHandler;
  }

  @Transactional(readOnly = true)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @GetMapping(value = "/{projectKey}/{dataId}")
  @Operation(summary = "Get file")
  public void getFile(@PathVariable String projectKey, @PathVariable("dataId") Long dataId,
      HttpServletResponse response,
      @AuthenticationPrincipal ReportPortalUser user) {
    toResponse(response, getFileHandler.loadFileById(dataId,
        projectExtractor.extractMembershipDetails(user, projectKey)));
  }


  @Transactional
  @PreAuthorize(IS_ADMIN)
  @PostMapping(value = "/clean", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(summary = "Remove attachments from file storage according to uploaded csv file")
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
		if (binaryData.getInputStream() != null) {
			response.setContentType(binaryData.getContentType());
			if (binaryData.getFileName() != null) {
				response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + binaryData.getFileName() + "\"");
			}
			try (InputStream inputStream = binaryData.getInputStream()) {
				IOUtils.copy(inputStream, response.getOutputStream());
			} catch (IOException e) {
				throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
			}
		} else {
			response.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}
}
