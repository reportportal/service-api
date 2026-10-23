package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseReviewComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsReviewCommentState;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseReviewCommentRepository extends JpaRepository<TmsTestCaseReviewComment, Long> {

  List<TmsTestCaseReviewComment> findByTestCaseIdOrderByCreatedAtAscIdAsc(Long testCaseId);

  List<TmsTestCaseReviewComment> findByTestCaseIdAndState(Long testCaseId, TmsReviewCommentState state);

  List<TmsTestCaseReviewComment> findByFixPushId(Long fixPushId);

  long countByTestCaseIdAndState(Long testCaseId, TmsReviewCommentState state);

  /**
   * The ids of the test cases among the given ones that have at least one comment in the given state.
   */
  @Query("SELECT DISTINCT c.testCaseId FROM TmsTestCaseReviewComment c "
      + "WHERE c.state = :state AND c.testCaseId IN :testCaseIds")
  List<Long> findTestCaseIdsWithState(@Param("testCaseIds") Collection<Long> testCaseIds,
      @Param("state") TmsReviewCommentState state);

  @Modifying
  @Query("DELETE FROM TmsTestCaseReviewComment c WHERE c.testCaseId = :testCaseId AND c.state = :state")
  int deleteByTestCaseIdAndState(@Param("testCaseId") Long testCaseId, @Param("state") TmsReviewCommentState state);
}
