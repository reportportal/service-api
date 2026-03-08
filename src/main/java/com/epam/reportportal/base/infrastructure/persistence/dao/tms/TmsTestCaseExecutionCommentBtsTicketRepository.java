package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentBtsTicket;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TMS test case execution comment bts tickets relationships.
 */
@Repository
public interface TmsTestCaseExecutionCommentBtsTicketRepository extends
    ReportPortalRepository<TmsTestCaseExecutionCommentBtsTicket, Long> {

  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentBtsTicket t WHERE t.comment.id = :commentId")
  void deleteByExecutionCommentId(@Param("commentId") Long commentId);

  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentBtsTicket t WHERE t.comment.execution.id = :executionId")
  void deleteByExecutionId(@Param("executionId") Long executionId);

  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionCommentBtsTicket t WHERE t.comment.execution.launchId = :launchId")
  void deleteByLaunchId(@Param("launchId") Long launchId);
}
