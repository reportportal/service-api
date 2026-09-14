package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.UploadAttachmentRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for managing TMS attachments.
 */
public interface TmsAttachmentService {

  /**
   * Uploads an attachment file and creates a TmsAttachment entity with TTL.
   *
   * @param projectId the project ID to scope the attachment storage key
   * @param file      the multipart file to upload
   * @return the created TmsAttachment with TTL set
   */
  UploadAttachmentRS uploadAttachment(Long projectId, MultipartFile file);

  /**
   * Downloads an attachment by ID, scoped to a project.
   *
   * @param projectId    the project ID the attachment must belong to
   * @param attachmentId the attachment ID
   * @return Optional containing the attachment if found within the given project
   */
  Optional<TmsAttachment> getTmsAttachment(Long projectId, Long attachmentId);

  /**
   * Deletes an attachment by ID, scoped to a project.
   *
   * @param projectId    the project ID the attachment must belong to
   * @param attachmentId the attachment ID
   */
  void deleteAttachment(Long projectId, Long attachmentId);

  /**
   * Removes TTL from attachments when they are permanently associated with test cases.
   *
   * @param attachmentIds list of attachment IDs
   */
  void removeTtlFromTmsAttachments(List<Long> attachmentIds);

  /**
   * Finds and deletes expired attachments.
   */
  void cleanupExpiredAttachments();

  /**
   * Validates that attachments exist and belong to the given project, and returns them.
   *
   * @param projectId     the project ID all attachments must belong to
   * @param attachmentIds list of attachment IDs to validate
   * @return list of valid attachments
   */
  List<TmsAttachment> getTmsAttachmentsByIds(Long projectId, List<Long> attachmentIds);

  /**
   * Finds attachments by IDs, scoped to a project. Unlike {@link #getTmsAttachmentsByIds}, attachment IDs that do not
   * exist or belong to another project are silently excluded from the result instead of throwing.
   *
   * @param projectId     the project ID attachments must belong to
   * @param attachmentIds list of attachment IDs to look up
   * @return list of attachments that exist and belong to the given project; missing/foreign IDs are omitted
   */
  List<TmsAttachment> findAvailableAttachments(Long projectId, List<Long> attachmentIds);

  TmsAttachment duplicateTmsAttachment(TmsAttachment originalAttachment);

  void setExpirationForUnusedAttachments();

  void saveAll(Collection<TmsAttachment> attachments);

  /**
   * Deletes all TMS attachment binary data (files and thumbnails) belonging to a project.
   *
   * @param projectId the project ID whose TMS attachment blobs should be removed
   */
  void deleteAllByProjectId(Long projectId);
}
