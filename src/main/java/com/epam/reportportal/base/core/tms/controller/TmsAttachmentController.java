package com.epam.reportportal.base.core.tms.controller;

import com.epam.reportportal.base.infrastructure.persistence.binary.tms.TmsAttachmentDataStoreService;
import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.core.tms.service.TmsAttachmentService;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for TMS attachment operations.
 *
 * Provides endpoints for uploading, downloading, and deleting TMS attachments
 * that can be used in test case preconditions and manual scenario steps.
 */
@Slf4j
@RestController
@RequestMapping("/v1/project/{projectKey}/tms/attachment")
@RequiredArgsConstructor
@Tag(name = "TMS Attachment Controller", description = "TMS Attachment Management")
public class TmsAttachmentController {

  private final TmsAttachmentService tmsAttachmentService;
  private final TmsAttachmentDataStoreService tmsAttachmentDataStoreService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload TMS attachment",
      description = "Uploads a file as TMS attachment with TTL for temporary storage")
  @PreAuthorize("hasPermission(#projectKey, 'allowedToEditProject')")
  public UploadAttachmentRS uploadAttachment(
      @Parameter(description = "Project key") @PathVariable String projectKey,
      @Parameter(description = "Attachment file") @RequestParam("file") MultipartFile file) {
    return tmsAttachmentService.uploadAttachment(file);
  }

  @GetMapping("/{attachmentId}")
  @Operation(summary = "Download TMS attachment",
      description = "Downloads TMS attachment file by ID")
  @PreAuthorize("hasPermission(#projectKey, 'allowedToViewProject')")
  public ResponseEntity<InputStreamResource> downloadAttachment(
      @Parameter(description = "Project key") @PathVariable String projectKey,
      @Parameter(description = "Attachment ID") @PathVariable Long attachmentId) {

    log.debug("Downloading TMS attachment: {} for project: {}", attachmentId, projectKey);

    var attachment = tmsAttachmentService.getTmsAttachment(attachmentId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Attachment not found: " + attachmentId));

    var resource = new InputStreamResource(
        tmsAttachmentDataStoreService
            .load(attachment.getPathToFile())
            .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
                "Attachment file not found: " + attachmentId))
    );

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + attachment.getFileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, attachment.getFileType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getFileSize()))
        .body(resource);
  }
  
  @GetMapping("/{attachmentId}/thumbnail")
  @Operation(summary = "Download TMS attachment thumbnail",
      description = "Downloads TMS attachment thumbnail file by ID")
  @PreAuthorize("hasPermission(#projectKey, 'allowedToViewProject')")
  public ResponseEntity<InputStreamResource> downloadThumbnail(
      @Parameter(description = "Project key") @PathVariable String projectKey,
      @Parameter(description = "Attachment ID") @PathVariable Long attachmentId) {

    log.debug("Downloading TMS attachment thumbnail: {} for project: {}", attachmentId, projectKey);

    var attachment = tmsAttachmentService.getTmsAttachment(attachmentId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
            "Attachment not found: " + attachmentId));

    if (attachment.getThumbnailPath() == null) {
      throw new ReportPortalException(ErrorType.NOT_FOUND, 
          "Thumbnail not found for attachment: " + attachmentId);
    }

    var resource = new InputStreamResource(
        tmsAttachmentDataStoreService
            .load(attachment.getThumbnailPath())
            .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
                "Attachment thumbnail file not found: " + attachmentId))
    );

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, attachment.getFileType())
        .body(resource);
  }

  @DeleteMapping("/{attachmentId}")
  @Operation(summary = "Delete TMS attachment",
      description = "Deletes TMS attachment by ID")
  @PreAuthorize("hasPermission(#projectKey, 'allowedToEditProject')")
  public ResponseEntity<OperationCompletionRS> deleteAttachment(
      @Parameter(description = "Project key") @PathVariable String projectKey,
      @Parameter(description = "Attachment ID") @PathVariable Long attachmentId) {

    log.debug("Deleting TMS attachment: {} for project: {}", attachmentId, projectKey);

    tmsAttachmentService.deleteAttachment(attachmentId);

    return ResponseEntity.ok(new OperationCompletionRS(
        "Attachment with ID = '" + attachmentId + "' successfully deleted."));
  }
}
