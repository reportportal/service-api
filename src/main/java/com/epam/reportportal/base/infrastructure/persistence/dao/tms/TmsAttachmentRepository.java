package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttachment;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TMS attachments. Now works with Many-to-Many relationships
 * through junction tables.
 */
@Repository
public interface TmsAttachmentRepository extends ReportPortalRepository<TmsAttachment, Long> {

  /**
   * Finds expired attachments for cleanup.
   */
  @Query("SELECT a FROM TmsAttachment a WHERE a.expiresAt IS NOT NULL AND a.expiresAt < :currentTime")
  List<TmsAttachment> findExpiredAttachments(@Param("currentTime") Instant currentTime);

  /**
   * Finds attachments that have no TTL set (expiresAt is null). These are candidates for TTL
   * assignment if they are unused.
   */
  @Query("SELECT a FROM TmsAttachment a WHERE a.expiresAt IS NULL")
  List<TmsAttachment> findAttachmentsWithoutTtl();

  /**
   * Sets expiration time for specified attachments.
   *
   * @param attachmentIds  list of attachment IDs to set expiration for
   * @param expirationTime the expiration timestamp
   * @return number of updated records
   */
  @Modifying
  @Query("UPDATE TmsAttachment a SET a.expiresAt = :expirationTime WHERE a.id IN :attachmentIds")
  int setExpirationForAttachments(@Param("attachmentIds") List<Long> attachmentIds,
      @Param("expirationTime") Instant expirationTime);

  /**
   * Removes expiration from attachments (makes them permanent).
   */
  @Modifying
  @Query("UPDATE TmsAttachment a SET a.expiresAt = null WHERE a.id IN :attachmentIds")
  void removeExpirationFromAttachments(@Param("attachmentIds") List<Long> attachmentIds);

  /**
   * Deletes attachments by IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsAttachment a WHERE a.id IN :attachmentIds")
  void deleteByIds(@Param("attachmentIds") List<Long> attachmentIds);
}
