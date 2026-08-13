/*
 * Copyright 2026 EPAM Systems
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

import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole.MANAGER;
import static com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole.ADMINISTRATOR;

import com.epam.reportportal.base.core.file.FileSignedLinkService;
import com.epam.reportportal.base.infrastructure.persistence.binary.AttachmentBinaryDataService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.BinaryData;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.ProjectExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Public attachment streaming API secured by short-lived HMAC signatures.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/public/data")
@Tag(name = "Public File Storage", description = "Public Files Storage API collection")
public class PublicFileStorageController {

  private final FileSignedLinkService fileSignedLinkService;
  private final UserRepository userRepository;
  private final ProjectExtractor projectExtractor;
  private final AttachmentBinaryDataService attachmentBinaryDataService;
  private final AttachmentStreamResponseFactory attachmentStreamResponseFactory;

  /**
   * Streams an attachment after validating the URL signature and the issuer's current access.
   *
   * @param dataId     attachment ID
   * @param projectKey signed project key
   * @param userId     signed issuing user ID
   * @param exp        signed expiration epoch seconds
   * @param signature  HMAC signature
   * @param request    HTTP request
   * @return full or partial attachment stream
   */
  @GetMapping("/streams/{dataId}")
  @Operation(summary = "Get public file stream")
  public ResponseEntity<StreamingResponseBody> getFileStream(@PathVariable("dataId") Long dataId,
      @RequestParam(value = "pk", required = false) String projectKey,
      @RequestParam(value = "uid", required = false) Long userId,
      @RequestParam(value = "exp", required = false) Long exp,
      @RequestParam(value = "sig", required = false) String signature, HttpServletRequest request) {
    var payload = fileSignedLinkService.verify(dataId, projectKey, userId, exp, signature);

    var user = userRepository.findReportPortalUser(payload.userId())
        .filter(ReportPortalUser::isEnabled).orElseThrow(PublicFileStorageController::accessDenied);

    MembershipDetails membership;
    if (ADMINISTRATOR.equals(user.getUserRole())) {
      membership = projectExtractor.extractProjectDetailsAdmin(payload.projectKey());
    } else {
      membership = projectExtractor.findMembershipDetails(payload.userId(), payload.projectKey())
          .filter(
              details -> MANAGER.equals(details.getOrgRole()) || details.getProjectRole() != null)
          .orElseThrow(PublicFileStorageController::accessDenied);
    }

    var metadata = attachmentBinaryDataService.getMetadataForStreaming(dataId, membership);
    var range = attachmentStreamResponseFactory.resolveRange(request, metadata.fileSize());
    if (range.type() == AttachmentStreamRange.Type.UNSATISFIABLE) {
      return attachmentStreamResponseFactory.toStreamingResponse(
          new BinaryData(metadata.fileName(), metadata.contentType(), metadata.fileSize(), null),
          range);
    }

    var binaryData = attachmentBinaryDataService.loadForStreaming(metadata, range.offset(),
        range.contentLength());
    return attachmentStreamResponseFactory.toStreamingResponse(binaryData, range);
  }

  private static ReportPortalException accessDenied() {
    return new ReportPortalException(ErrorType.ACCESS_DENIED,
        "Please check the list of your available projects.");
  }
}
