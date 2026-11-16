package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentAttachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentAttachmentId;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TMS test case execution comment attachment relationships.
 */
@Repository
public interface TmsTestCaseExecutionCommentAttachmentRepository extends
    ReportPortalRepository<TmsTestCaseExecutionCommentAttachment, TmsTestCaseExecutionCommentAttachmentId> {

  /**
   * Deletes attachment relationships by execution comment ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentAttachment tca WHERE tca.executionComment.id = :executionCommentId")
  void deleteByExecutionCommentId(@Param("executionCommentId") Long executionCommentId);

  /**
   * Deletes attachment relationships by execution comment IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentAttachment tca WHERE tca.executionComment.id IN (:executionCommentIds)")
  void deleteByExecutionCommentIds(@Param("executionCommentIds") List<Long> executionCommentIds);

  /**
   * Deletes attachment relationships by execution ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentAttachment tca WHERE tca.executionComment.execution.id = :executionId")
  void deleteByExecutionId(@Param("executionId") Long executionId);

  /**
   * Deletes attachment relationships by execution IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentAttachment tca WHERE tca.executionComment.execution.id IN (:executionIds)")
  void deleteByExecutionIds(@Param("executionIds") List<Long> executionIds);

  /**
   * Deletes attachment relationships by launch ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentAttachment tca WHERE tca.executionComment.execution.launchId = :launchId")
  void deleteByLaunchId(@Param("launchId") Long launchId);

  /**
   * Finds all attachment IDs for TTL management.
   */
  @Query("SELECT DISTINCT tca.attachment.id FROM TmsTestCaseExecutionCommentAttachment tca")
  List<Long> findAllAttachmentIds();
}
