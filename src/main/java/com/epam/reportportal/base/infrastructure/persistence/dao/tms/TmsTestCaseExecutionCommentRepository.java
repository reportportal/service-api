package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing TMS test case execution comments.
 */
@Repository
public interface TmsTestCaseExecutionCommentRepository extends
    ReportPortalRepository<TmsTestCaseExecutionComment, Long> {

  /**
   * Finds execution comment by execution ID.
   */
  Optional<TmsTestCaseExecutionComment> findByExecutionId(Long executionId);

  /**
   * Finds all execution comments by execution IDs.
   */
  List<TmsTestCaseExecutionComment> findByExecutionIdIn(List<Long> executionIds);

  /**
   * Finds all execution comments by launch ID.
   */
  @Query("SELECT tec FROM TmsTestCaseExecutionComment tec WHERE tec.execution.launchId = :launchId")
  List<TmsTestCaseExecutionComment> findByLaunchId(@Param("launchId") Long launchId);

  /**
   * Deletes execution comment by execution ID.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionComment tec WHERE tec.execution.id = :executionId")
  void deleteByExecutionId(@Param("executionId") Long executionId);

  /**
   * Deletes execution comments by execution IDs.
   */
  @Modifying
  @Query("DELETE FROM TmsTestCaseExecutionComment tec WHERE tec.execution.id IN (:executionIds)")
  void deleteByExecutionIds(@Param("executionIds") List<Long> executionIds);

  /**
   * Deletes execution comments by launch ID.
   */
  @Modifying
  @Query(value = """
      DELETE FROM tms_test_case_execution_comment 
      WHERE execution_id IN (
        SELECT id FROM tms_test_case_execution WHERE launch_id = :launchId
      )
      """, nativeQuery = true)
  void deleteByLaunchId(@Param("launchId") Long launchId);

  /**
   * Checks if execution comment exists for execution.
   */
  boolean existsByExecutionId(Long executionId);
}
