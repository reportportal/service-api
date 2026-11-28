package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsMilestoneRepository extends ReportPortalRepository<TmsMilestone, Long> {

  /**
   * Finds a milestone by its ID and project ID.
   *
   * @param id        the milestone ID
   * @param projectId the project ID
   * @return optional milestone
   */
  @Query("SELECT m FROM TmsMilestone m WHERE m.id = :id AND m.project.id = :projectId")
  Optional<TmsMilestone> findByIdAndProjectId(@Param("id") Long id,
      @Param("projectId") Long projectId);

  /**
   * Finds all milestones by project ID with pagination.
   *
   * @param projectId the project ID
   * @param pageable  pagination parameters
   * @return page of milestones
   */
  @Query("SELECT m FROM TmsMilestone m WHERE m.project.id = :projectId")
  Page<TmsMilestone> findAllByProjectId(@Param("projectId") Long projectId, Pageable pageable);

  /**
   * Checks if a milestone exists by ID and project ID.
   *
   * @param id        the milestone ID
   * @param projectId the project ID
   * @return true if exists, false otherwise
   */
  @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM TmsMilestone m "
      + "WHERE m.id = :id AND m.project.id = :projectId")
  boolean existsByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

  /**
   * Deletes a milestone by ID and project ID.
   *
   * @param id        the milestone ID
   * @param projectId the project ID
   * @return number of deleted records
   */
  @Modifying
  @Query("DELETE FROM TmsMilestone m WHERE m.id = :id AND m.project.id = :projectId")
  int deleteByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

  /**
   * Checks whether a test execution of any test plan of the milestone has a status other than
   * {@code TO_RUN}.
   *
   * @param milestoneId the milestone ID
   * @return true if at least one test of the milestone was executed
   */
  @Query(value = "SELECT EXISTS (SELECT 1 FROM tms_test_plan tp "
      + "JOIN launch l ON l.test_plan_id = tp.id "
      + "JOIN tms_test_case_execution e ON e.launch_id = l.id "
      + "JOIN test_item_results r ON r.result_id = e.test_item_id "
      + "WHERE tp.milestone_id = :milestoneId AND r.status <> 'TO_RUN')",
      nativeQuery = true)
  boolean existsExecutedTestByMilestoneId(@Param("milestoneId") Long milestoneId);

  /**
   * Moves Scheduled milestones with the "start on the start date" option to Testing once their
   * start date is reached.
   *
   * @param now the current time (UTC)
   * @return number of updated milestones
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "UPDATE tms_milestone SET status = 'TESTING' "
      + "WHERE status = 'SCHEDULED' AND auto_start_on_start_date "
      + "AND start_date IS NOT NULL AND start_date <= :now",
      nativeQuery = true)
  int startScheduledOnStartDate(@Param("now") LocalDateTime now);

  /**
   * Moves Scheduled milestones with the "start with the first execution" option to Testing when a
   * test execution of any of their test plans has a status other than {@code TO_RUN}.
   *
   * @return number of updated milestones
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "UPDATE tms_milestone m SET status = 'TESTING' "
      + "WHERE m.status = 'SCHEDULED' AND m.auto_start_on_first_execution "
      + "AND EXISTS (SELECT 1 FROM tms_test_plan tp "
      + "JOIN launch l ON l.test_plan_id = tp.id "
      + "JOIN tms_test_case_execution e ON e.launch_id = l.id "
      + "JOIN test_item_results r ON r.result_id = e.test_item_id "
      + "WHERE tp.milestone_id = m.id AND r.status <> 'TO_RUN')",
      nativeQuery = true)
  int startScheduledWithExecutedTests();

  /**
   * Moves the Scheduled milestone of the launch's test plan to Testing if it has the "start with
   * the first execution" option.
   *
   * @param launchId the launch ID of the executed test
   * @return number of updated milestones (0 or 1)
   */
  // No clearAutomatically: runs inside the execution PATCH, whose entities must stay managed.
  @Modifying(flushAutomatically = true)
  @Query(value = "UPDATE tms_milestone m SET status = 'TESTING' "
      + "FROM tms_test_plan tp JOIN launch l ON l.test_plan_id = tp.id "
      + "WHERE l.id = :launchId AND tp.milestone_id = m.id "
      + "AND m.status = 'SCHEDULED' AND m.auto_start_on_first_execution",
      nativeQuery = true)
  int startScheduledByLaunchId(@Param("launchId") Long launchId);

  /**
   * Moves Testing milestones with the "complete after the deadline" option to Completed once their
   * deadline day is over.
   *
   * @param startOfToday the start of the current day (UTC); a deadline before it is over
   * @return number of updated milestones
   */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "UPDATE tms_milestone SET status = 'COMPLETED' "
      + "WHERE status = 'TESTING' AND auto_complete_after_deadline "
      + "AND end_date IS NOT NULL AND end_date < :startOfToday",
      nativeQuery = true)
  int completeTestingAfterDeadline(@Param("startOfToday") LocalDateTime startOfToday);
}
