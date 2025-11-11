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

package com.epam.reportportal.ws.controller;

import static com.epam.reportportal.auth.permissions.Permissions.ALLOWED_TO_VIEW_PROJECT;
import static com.epam.reportportal.auth.permissions.Permissions.IS_ADMIN;

import com.epam.reportportal.core.file.DeleteFilesHandler;
import com.epam.reportportal.core.file.GetFileHandler;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.reporting.OperationCompletionRS;
import com.epam.reportportal.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * File storage controller.
 *
 * @author Dzianis_Shybeka
 */
@RestController
@RequestMapping("/v1/data")
@Tag(name = "File Storage", description = "Files Storage API collection")
public class FileStorageController {

  private final ProjectExtractor projectExtractor;
  private final GetFileHandler getFileHandler;
  private final DeleteFilesHandler deleteFilesHandler;

  /**
   * Constructor with mandatory fields.
   */
  @Autowired
  public FileStorageController(
      ProjectExtractor projectExtractor,
      GetFileHandler getFileHandler,
      DeleteFilesHandler deleteFilesHandler
  ) {
    this.projectExtractor = projectExtractor;
    this.getFileHandler = getFileHandler;
    this.deleteFilesHandler = deleteFilesHandler;
  }

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
  public void getFile(
      @PathVariable String projectKey,
      @PathVariable("dataId") Long dataId,
      HttpServletResponse response,
      @AuthenticationPrincipal ReportPortalUser user
  ) {
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
  public ResponseEntity<StreamingResponseBody> getFileStream(
      @PathVariable String projectKey,
      @PathVariable("dataId") Long dataId,
      @AuthenticationPrincipal ReportPortalUser user,
      HttpServletRequest request
  ) {
    var membership = projectExtractor.extractMembershipDetails(user, projectKey);
    var binaryData = getFileHandler.loadFileById(dataId, membership);
    return toStreamingResponse(binaryData, request);
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
  public OperationCompletionRS removeAttachmentsByCsv(
      @RequestParam("file") MultipartFile file
  ) {
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

  private ResponseEntity<StreamingResponseBody> toStreamingResponse(
      BinaryData binaryData,
      HttpServletRequest request
  ) {
    final var binaryStream = binaryData.getInputStream();
    if (binaryStream == null) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    var headers = new HttpHeaders();

    headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");

    Optional.ofNullable(binaryData.getContentType())
        .map(MediaType::parseMediaType)
        .ifPresent(headers::setContentType);

    Optional.ofNullable(binaryData.getFileName())
        .map(n -> ContentDisposition.builder("inline").filename(n).build())
        .ifPresent(headers::setContentDisposition);

    var fileLength = binaryData.getLength();
    var requestHeader = request.getHeader(HttpHeaders.RANGE);

    HttpRange range = null;
    if (fileLength != null && requestHeader != null && !requestHeader.isBlank()) {
      range = parseRange(requestHeader);
    }

    if (range == null) {
      if (fileLength != null) {
        headers.setContentLength(fileLength);
      }

      StreamingResponseBody responseBody = outputStream -> {
        try (InputStream inputStream = binaryStream) {
          IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
          throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
        }
      };

      return ResponseEntity.ok()
          .headers(headers)
          .body(responseBody);
    }

    long rangeStart = range.getRangeStart(fileLength);
    long rangeEnd = Math.min(range.getRangeEnd(fileLength), fileLength - 1);

    if (rangeStart >= fileLength || rangeStart > rangeEnd) {
      try {
        binaryStream.close();
      } catch (IOException ignored) {
      }

      headers.set(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength);
      return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
          .headers(headers)
          .build();
    }

    headers.set(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
    headers.setContentLength(rangeEnd - rangeStart + 1);

    StreamingResponseBody responseBody = outputStream -> {
      try (InputStream inputStream = binaryStream) {
        StreamUtils.copyRange(inputStream, outputStream, rangeStart, rangeEnd);
      } catch (IOException e) {
        throw new ReportPortalException("Unable to retrieve binary data from data storage", e);
      }
    };

    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
        .headers(headers)
        .body(responseBody);
  }

  private HttpRange parseRange(String rangeHeader) {
    try {
      var ranges = HttpRange.parseRanges(rangeHeader);
      return ranges.size() == 1 ? ranges.getFirst() : null;
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
