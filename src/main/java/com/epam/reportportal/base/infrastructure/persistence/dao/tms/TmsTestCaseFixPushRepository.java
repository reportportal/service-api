package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseFixPush;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsFixPushStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseFixPushRepository extends JpaRepository<TmsTestCaseFixPush, Long> {

  Optional<TmsTestCaseFixPush> findByTestCaseIdAndStatus(Long testCaseId, TmsFixPushStatus status);

  boolean existsByTestCaseIdAndStatus(Long testCaseId, TmsFixPushStatus status);

  List<TmsTestCaseFixPush> findByTestCaseIdOrderByRoundNumberAsc(Long testCaseId);

  @Query("SELECT COALESCE(MAX(p.roundNumber), 0) FROM TmsTestCaseFixPush p WHERE p.testCaseId = :testCaseId")
  int findLastRoundNumber(@Param("testCaseId") Long testCaseId);

  /**
   * The ids of the test cases among the given ones that have a round in the given status.
   */
  @Query("SELECT p.testCaseId FROM TmsTestCaseFixPush p WHERE p.status = :status AND p.testCaseId IN :testCaseIds")
  List<Long> findTestCaseIdsWithStatus(@Param("testCaseIds") Collection<Long> testCaseIds,
      @Param("status") TmsFixPushStatus status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM TmsTestCaseFixPush p WHERE p.id = :id")
  Optional<TmsTestCaseFixPush> findByIdForUpdate(@Param("id") Long id);

  /**
   * Rounds in the given status that were requested before the given moment, e.g. the pending ones the agent
   * did not answer in time.
   */
  List<TmsTestCaseFixPush> findByStatusAndRequestedAtBefore(TmsFixPushStatus status, Instant requestedBefore);
}
