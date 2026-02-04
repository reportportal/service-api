package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for {@link TmsTestCaseExecution} entity.
 */
public interface TmsTestCaseExecutionRepository extends
    ReportPortalRepository<TmsTestCaseExecution, Long> {

  @Query("""
      SELECT e
      FROM TmsTestCaseExecution e
      JOIN TestItem ti ON ti.itemId = e.testItem.itemId
      JOIN Launch l ON l.id = ti.launchId
      JOIN TmsTestPlan tp ON tp.id = l.testPlanId
      WHERE e.testCaseId IN :testCaseIds
        AND tp.id = :testPlanId
      ORDER BY ti.startTime DESC
      """)
  List<TmsTestCaseExecution> findByTestCaseIdsAndTestPlanId(
      @Param("testCaseIds") List<Long> testCaseIds,
      @Param("testPlanId") Long testPlanId
  );

  @Query("""
      SELECT e
      FROM TmsTestCaseExecution e
      JOIN TestItem ti ON ti.itemId = e.testItem.itemId
      JOIN Launch l ON l.id = ti.launchId
      WHERE e.testCaseId IN :testCaseIds
      ORDER BY ti.startTime DESC
      """)
  List<TmsTestCaseExecution> findByTestCaseIds(
      @Param("testCaseIds") List<Long> testCaseIds
  );

  @Query("""
      SELECT e
      FROM TmsTestCaseExecution e
      JOIN TestItem ti ON ti.itemId = e.testItem.itemId
      JOIN Launch l ON l.id = ti.launchId
      JOIN TmsTestPlan tp ON tp.id = l.testPlanId
      WHERE e.testCaseId = :testCaseId
            AND tp.id = :testPlanId
      ORDER BY ti.startTime DESC
      """)
  List<TmsTestCaseExecution> findByTestCaseIdAndTestPlanId(
      @Param("testCaseId") Long testCaseId,
      @Param("testPlanId") Long testPlanId
  );

  @Query("""
      SELECT e
      FROM TmsTestCaseExecution e
      JOIN TestItem ti ON ti.itemId = e.testItem.itemId
      JOIN Launch l ON l.id = ti.launchId
      WHERE e.testCaseId = :testCaseId
      ORDER BY ti.startTime DESC
      """)
  List<TmsTestCaseExecution> findByTestCaseId(
      @Param("testCaseId") Long testCaseId
  );

  @Query(value = """
      SELECT DISTINCT ON (e.test_case_id) e.*
      FROM tms_test_case_execution e
      JOIN test_item ti ON ti.item_id = e.test_item_id
      JOIN launch l ON l.id = ti.launch_id
      JOIN tms_test_plan tp ON tp.id = l.test_plan_id
      WHERE e.test_case_id IN :testCaseIds
        AND tp.id = :testPlanId
      ORDER BY e.test_case_id, ti.start_time DESC
      """, nativeQuery = true)
  List<TmsTestCaseExecution> findLastExecutionsByTestCaseIdsAndTestPlanId(
      @Param("testCaseIds") List<Long> testCaseIds,
      @Param("testPlanId") Long testPlanId
  );

  @Query(value = """
      SELECT DISTINCT ON (e.test_case_id) e.*
      FROM tms_test_case_execution e
      JOIN test_item ti ON ti.item_id = e.test_item_id
      JOIN launch l ON l.id = ti.launch_id
      WHERE e.test_case_id IN :testCaseIds
      ORDER BY e.test_case_id, ti.start_time DESC
      """, nativeQuery = true)
  List<TmsTestCaseExecution> findLastExecutionsByTestCaseIds(
      @Param("testCaseIds") List<Long> testCaseIds
  );

  @Query(value = """
      SELECT e.*
      FROM tms_test_case_execution e
      JOIN test_item ti ON ti.item_id = e.test_item_id
      JOIN launch l ON l.id = ti.launch_id
      WHERE e.test_case_id = :testCaseId
      ORDER BY e.test_case_id, ti.start_time DESC
      LIMIT 1      
      """, nativeQuery = true)
  Optional<TmsTestCaseExecution> findLastExecutionByTestCaseId(
      @Param("testCaseId") Long testCaseId
  );

  @Query(value = """
      SELECT e.*
      FROM tms_test_case_execution e
      JOIN test_item ti ON ti.item_id = e.test_item_id
      JOIN launch l ON l.id = ti.launch_id
      JOIN tms_test_plan tp ON tp.id = l.test_plan_id
      WHERE e.test_case_id = :testCaseId
        AND tp.id = :testPlanId
      ORDER BY e.test_case_id, ti.start_time DESC
      LIMIT 1      
      """, nativeQuery = true)
  Optional<TmsTestCaseExecution> findLastExecutionByTestCaseIdAndTestPlanId(
      @Param("testCaseId") Long testCaseId,
      @Param("testPlanId") Long testPlanId
  );


  /**
   * Finds execution by test item ID.
   *
   * @param testItemId test item ID
   * @return optional test case execution
   */
  @Query("SELECT e FROM TmsTestCaseExecution e WHERE e.testItem.itemId = :testItemId")
  Optional<TmsTestCaseExecution> findByTestItemId(@Param("testItemId") Long testItemId);

//  /**
//   * Finds all executions by launch ID.
//   *
//   * @param launchId launch ID
//   * @return list of executions
//   */
//  @Query("SELECT e FROM TmsTestCaseExecution e WHERE e.launchId = :launchId")
//  List<TmsTestCaseExecution> findByLaunchId(@Param("launchId") Long launchId);
//
//  /**
//   * Finds execution by test case execution ID and launch ID.
//   *
//   * @param testCaseExecutionId test case execution ID
//   * @param launchId   launch ID
//   * @return optional execution
//   */
//  @Query("SELECT e FROM TmsTestCaseExecution e "
//      + "WHERE e.id = :testCaseExecutionId "
//      + "AND e.launchId = :launchId")
//  Optional<TmsTestCaseExecution> findByTestCaseExecutionIdAndLaunchId(
//      @Param("testCaseExecutionId") Long testCaseExecutionId,
//      @Param("launchId") Long launchId
//  );
//
//  /**
//   * Checks if execution exists for test case in launch.
//   *
//   * @param testCaseId test case ID
//   * @param launchId   launch ID
//   * @return true if exists
//   */
//  @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM TmsTestCaseExecution e WHERE e.testCaseId = :testCaseId AND e.launchId = :launchId")
//  boolean existsByTestCaseIdAndLaunchId(
//      @Param("testCaseId") Long testCaseId,
//      @Param("launchId") Long launchId
//  );
//
//  /**
//   * Deletes all executions by launch ID.
//   *
//   * @param launchId launch ID
//   */
//  @Modifying
//  @Query("DELETE FROM TmsTestCaseExecution e WHERE e.launchId = :launchId")
//  void deleteByLaunchId(@Param("launchId") Long launchId);
//
//  /**
//   * Deletes execution by test case ID and launch ID.
//   *
//   * @param testCaseId test case ID
//   * @param launchId   launch ID
//   */
//  @Modifying
//  @Query("DELETE FROM TmsTestCaseExecution e WHERE e.testCaseId = :testCaseId AND e.launchId = :launchId")
//  void deleteByTestCaseIdAndLaunchId(
//      @Param("testCaseId") Long testCaseId,
//      @Param("launchId") Long launchId
//  );
//
//  /**
//   * Checks if execution exists for test item.
//   *
//   * @param testItemId test item ID
//   * @return true if exists
//   */
//  @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM TmsTestCaseExecution e WHERE e.testItem.itemId = :testItemId")
//  boolean existsByTestItemId(@Param("testItemId") Long testItemId);
//
//  /**
//   * Counts executions in launch.
//   *
//   * @param launchId launch ID
//   * @return count of executions
//   */
//  @Query("SELECT COUNT(e) FROM TmsTestCaseExecution e WHERE e.launchId = :launchId")
//  Long countByLaunchId(@Param("launchId") Long launchId);
//
//  /**
//   * Finds executions by launch ID with test item loaded.
//   *
//   * @param launchId launch ID
//   * @return list of executions with test items
//   */
//  @Query("SELECT e FROM TmsTestCaseExecution e LEFT JOIN FETCH e.testItem WHERE e.launchId = :launchId")
//  List<TmsTestCaseExecution> findByLaunchIdWithTestItem(@Param("launchId") Long launchId);
//
//  /**
//   * Finds executions by launch ID ordered by creation.
//   *
//   * @param launchId launch ID
//   * @return list of executions ordered by test item start time
//   */
//  @Query("""
//      SELECT e
//      FROM TmsTestCaseExecution e
//      JOIN e.testItem ti
//      WHERE e.launchId = :launchId
//      ORDER BY ti.startTime ASC
//      """)
//  List<TmsTestCaseExecution> findByLaunchIdOrderByStartTime(@Param("launchId") Long launchId);
//
//  /**
//   * Finds executions by multiple launch IDs.
//   *
//   * @param launchIds list of launch IDs
//   * @return list of executions
//   */
//  @Query("SELECT e FROM TmsTestCaseExecution e WHERE e.launchId IN :launchIds")
//  List<TmsTestCaseExecution> findByLaunchIdIn(@Param("launchIds") List<Long> launchIds);
//
//  /**
//   * Counts executions by test case ID.
//   *
//   * @param testCaseId test case ID
//   * @return count of executions
//   */
//  @Query("SELECT COUNT(e) FROM TmsTestCaseExecution e WHERE e.testCaseId = :testCaseId")
//  Long countByTestCaseId(@Param("testCaseId") Long testCaseId);
//
//  /**
//   * Finds all executions for specific test cases in a launch.
//   *
//   * @param launchId    launch ID
//   * @param testCaseIds list of test case IDs
//   * @return list of executions
//   */
//  @Query("SELECT e FROM TmsTestCaseExecution e WHERE e.launchId = :launchId AND e.testCaseId IN :testCaseIds")
//  List<TmsTestCaseExecution> findByLaunchIdAndTestCaseIdIn(
//      @Param("launchId") Long launchId,
//      @Param("testCaseIds") List<Long> testCaseIds
//  );
//
//  /**
//   * Finds executions by launch ID with full details (test item and version).
//   *
//   * @param launchId launch ID
//   * @return list of executions with associations loaded
//   */
//  @Query("""
//      SELECT e
//      FROM TmsTestCaseExecution e
//      LEFT JOIN FETCH e.testItem ti
//      WHERE e.launchId = :launchId
//      ORDER BY ti.startTime ASC
//      """)
//  List<TmsTestCaseExecution> findByLaunchIdWithDetails(@Param("launchId") Long launchId);
}
