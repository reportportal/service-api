/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.ws.controller;

import static com.epam.reportportal.base.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.reportportal.base.auth.permissions.Permissions.IS_ADMIN;

import com.epam.reportportal.base.core.file.DeleteFilesHandler;
import com.epam.reportportal.base.core.file.FileSignedLinkService;
import com.epam.reportportal.base.core.file.GetFileHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.SignedFileLinkRs;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * File storage controller.
 *
 * @author Dzianis_Shybeka
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/data")
@Tag(name = "File Storage", description = "Files Storage API collection")
public class FileStorageController {

  private final ProjectExtractor projectExtractor;
  private final GetFileHandler getFileHandler;
  private final DeleteFilesHandler deleteFilesHandler;
  private final FileSignedLinkService fileSignedLinkService;
  private final AttachmentStreamResponseFactory attachmentStreamResponseFactory;

  /**
   * Get file by its ID.
   *
   * @param projectKey Project key
   * @param dataId     File ID
   * @param response   Http response
   * @param user       Current user
   */
  @Transactional(readOnly = true)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @GetMapping(value = "/{projectKey}/{dataId}")
  @Operation(summary = "Get file")
  public void getFile(@PathVariable String projectKey, @PathVariable("dataId") Long dataId,
      HttpServletResponse response, @AuthenticationPrincipal ReportPortalUser user) {
    var membership = projectExtractor.extractMembershipDetails(user, projectKey);
    var binaryData = getFileHandler.loadFileById(dataId, membership);
    toResponse(response, binaryData);
  }

  /**
   * Get file stream by its ID.
   *
   * @param projectKey Project key
   * @param dataId     File ID
   * @param user       Current user
   * @param request    Http request
   * @return file stream response entity
   */
  @Transactional(readOnly = true)
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @GetMapping(value = "/{projectKey}/streams/{dataId}")
  @Operation(summary = "Get file stream")
  public ResponseEntity<StreamingResponseBody> getFileStream(@PathVariable String projectKey,
      @PathVariable("dataId") Long dataId, @AuthenticationPrincipal ReportPortalUser user,
      HttpServletRequest request) {
    var membership = projectExtractor.extractMembershipDetails(user, projectKey);
    var metadata = getFileHandler.getFileMetadataForStreaming(dataId, membership);
    var range = attachmentStreamResponseFactory.resolveRange(request, metadata.fileSize());
    if (range.type() == AttachmentStreamRange.Type.UNSATISFIABLE) {
      return attachmentStreamResponseFactory.toStreamingResponse(
          new BinaryData(metadata.fileName(), metadata.contentType(), metadata.fileSize(), null),
          range);
    }

    var binaryData = getFileHandler.loadFileForStreaming(metadata, range.offset(),
        range.contentLength());
    if (range.type() == AttachmentStreamRange.Type.FULL) {
      return toStreamingResponse(binaryData, request);
    }
    return attachmentStreamResponseFactory.toStreamingResponse(binaryData, range);
  }

  /**
   * Creates a signed public stream URL for an attachment.
   *
   * @param projectKey Project key
   * @param dataId     File ID
   * @param user       Current user
   * @return signed stream URL and expiration time
   */
  @PreAuthorize(ALLOWED_TO_VIEW_PROJECT)
  @PostMapping("/{projectKey}/streams/{dataId}/link")
  @Operation(summary = "Create public file stream link")
  public SignedFileLinkRs createSignedLink(@PathVariable String projectKey,
      @PathVariable("dataId") Long dataId, @AuthenticationPrincipal ReportPortalUser user) {
    var membership = projectExtractor.extractMembershipDetails(user, projectKey);
    var signedLink = fileSignedLinkService.createLink(dataId, membership.getProjectKey(),
        user.getUserId());
    var url = UriComponentsBuilder.fromPath("/v1/public/data/streams/{dataId}")
        .queryParam("pk", membership.getProjectKey()).queryParam("uid", user.getUserId())
        .queryParam("exp", signedLink.expiresAtEpochSecond())
        .queryParam("sig", signedLink.signature()).buildAndExpand(dataId).encode().toUriString();

    return new SignedFileLinkRs(url, Instant.ofEpochSecond(signedLink.expiresAtEpochSecond()));
  }

  /**
   * Remove attachments from file storage according to uploaded csv file.
   *
   * @param file Csv file with attachment ids to remove
   * @return Operation completion response
   */
  @Transactional
  @PreAuthorize(IS_ADMIN)
  @PostMapping(value = "/clean", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(summary = "Remove attachments from file storage according to uploaded csv file")
  public OperationCompletionRS removeAttachmentsByCsv(@RequestParam("file") MultipartFile file) {
    return deleteFilesHandler.removeFilesByCsv(file);
  }

  /**
   * Copies data from provided {@link InputStream} to Response.
   *
   * @param response   Response
   * @param binaryData Stored data
   */
  private void toResponse(HttpServletResponse response, BinaryData binaryData) {
    if (binaryData.getInputStream() != null) {
      response.setContentType(binaryData.getContentType());
      if (binaryData.getFileName() != null) {
        response.setHeader(com.google.common.net.HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + sanitizeFileName(binaryData.getFileName()) + "\"");
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

  private ResponseEntity<StreamingResponseBody> toStreamingResponse(BinaryData binaryData,
      HttpServletRequest request) {
    return attachmentStreamResponseFactory.toStreamingResponse(binaryData, request);
  }

  private String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[\n\r]+", " ");
  }

}
