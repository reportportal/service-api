package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TMS attachments. Now works with Many-to-Many relationships through junction
 * tables.
 */
@Repository
public interface TmsAttachmentRepository extends ReportPortalRepository<TmsAttachment, Long> {

  /**
   * Finds expired attachments for cleanup.
   */
  @Query("SELECT a FROM TmsAttachment a WHERE a.expiresAt IS NOT NULL AND a.expiresAt < :currentTime")
  List<TmsAttachment> findExpiredAttachments(@Param("currentTime") Instant currentTime);

  /**
   * Sets the expiration time on every attachment without TTL that is not referenced by any step,
   * text manual scenario, preconditions or test case execution comment.
   *
   * @param expirationTime expiration time to set
   * @return number of updated attachments
   */
  @Modifying
  @Query(value = "UPDATE tms_attachment a SET expires_at = :expirationTime "
      + "WHERE a.expires_at IS NULL "
      + "AND NOT EXISTS (SELECT 1 FROM tms_step_attachment x WHERE x.attachment_id = a.id) "
      + "AND NOT EXISTS (SELECT 1 FROM tms_text_manual_scenario_attachment x "
      + "  WHERE x.attachment_id = a.id) "
      + "AND NOT EXISTS (SELECT 1 FROM tms_manual_scenario_preconditions_attachment x "
      + "  WHERE x.attachment_id = a.id) "
      + "AND NOT EXISTS (SELECT 1 FROM tms_test_case_execution_comment_attachment x "
      + "  WHERE x.attachment_id = a.id)",
      nativeQuery = true)
  int setExpirationForUnusedAttachments(@Param("expirationTime") Instant expirationTime);

  /**
   * Deletes an attachment only if it is still expired (linking an attachment clears its
   * expiration).
   *
   * @param attachmentId attachment id
   * @param currentTime  current time
   * @return number of deleted rows
   */
  @Modifying
  @Query("DELETE FROM TmsAttachment a WHERE a.id = :attachmentId "
      + "AND a.expiresAt IS NOT NULL AND a.expiresAt < :currentTime")
  int deleteIfExpired(@Param("attachmentId") Long attachmentId,
      @Param("currentTime") Instant currentTime);

  /**
   * Removes expiration from attachments (makes them permanent).
   */
  @Modifying
  @Query("UPDATE TmsAttachment a SET a.expiresAt = null WHERE a.id IN :attachmentIds")
  void removeExpirationFromAttachments(@Param("attachmentIds") List<Long> attachmentIds);

  /**
   * Finds an attachment by ID, scoped to a project. Used to prevent cross-project access to attachments referenced by
   * numeric ID alone.
   */
  Optional<TmsAttachment> findByIdAndProjectId(Long id, Long projectId);

  /**
   * Finds attachments by IDs, scoped to a project. Attachment IDs belonging to other projects are silently excluded
   * from the result.
   */
  List<TmsAttachment> findAllByIdInAndProjectId(Collection<Long> ids, Long projectId);

  /**
   * Finds all attachments belonging to a project. Used when purging a project's TMS attachment storage.
   */
  List<TmsAttachment> findAllByProjectId(Long projectId);
}
