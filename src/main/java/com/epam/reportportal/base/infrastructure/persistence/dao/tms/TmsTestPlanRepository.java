package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.projection.TmsTestPlanName;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestPlanRepository extends ReportPortalRepository<TmsTestPlan, Long> {

  @Query("SELECT tp FROM TmsTestPlan tp " +
      "LEFT JOIN FETCH tp.attributes atr " +
      "LEFT JOIN FETCH atr.attribute " +
      "WHERE tp.project.id = :projectId AND tp.id = :id"
  )
  Optional<TmsTestPlan> findByIdAndProjectId(Long id, Long projectId);

  @Modifying
  @Query(value = "DELETE FROM TmsTestPlan tp "
      + "WHERE tp.id = :testPlanId "
      + "AND tp.project.id = :projectId")
  void deleteByIdAndProjectId(@Param("testPlanId") Long testPlanId,
      @Param("projectId") Long projectId);

  @Query(value = "SELECT tp.id FROM tms_test_plan tp " +
      "WHERE tp.project_id = :projectId " +
      "AND (:search IS NULL OR tp.search_vector @@ plainto_tsquery('simple', :search))",
      countQuery = "SELECT COUNT(tp.id) FROM tms_test_plan tp " +
          "WHERE tp.project_id = :projectId " +
          "AND (:search IS NULL OR tp.search_vector @@ plainto_tsquery('simple', :search))",
      nativeQuery = true)
  Page<Long> findIdsByCriteria(@Param("projectId") Long projectId,
      @Param("search") String search,
      Pageable pageable);

  @Query("SELECT tp FROM TmsTestPlan tp " +
      "LEFT JOIN FETCH tp.attributes atr " +
      "LEFT JOIN FETCH atr.attribute " +
      "WHERE tp.id IN :ids")
  List<TmsTestPlan> findByIdsWithAttributes(@Param("ids") List<Long> ids);

  @Query("SELECT tp.id FROM TmsTestPlan tp " +
      "WHERE tp.milestone.id = :milestoneId")
  List<Long> findIdsByProjectIdAndMilestoneId(
      @Param("projectId") Long projectId, @Param("milestoneId") Long milestoneId
  );

  @Query("SELECT tp.id FROM TmsTestPlan tp " +
      "WHERE tp.milestone.id in :milestoneIds")
  List<Long> findIdsByProjectIdAndMilestoneIds(
      @Param("projectId") Long projectId, @Param("milestoneIds") List<Long> milestoneIds
  );

  Boolean existsByIdAndProject_Id(Long testPlanId, Long projectId);

  /**
   * Find test plans by their IDs
   * @param testPlanIds list of test plan IDs
   * @return list of test plans
   */
  @Query("SELECT tp FROM TmsTestPlan tp WHERE tp.id IN :testPlanIds")
  List<TmsTestPlan> findByIds(@Param("testPlanIds") List<Long> testPlanIds);

  /**
   * Removes test plan from milestone by setting milestone to null.
   *
   * @param milestoneId the milestone ID
   * @param testPlanId  the test plan ID
   * @param projectId   the project ID
   * @return number of updated records
   */
  @Modifying
  @Query("UPDATE TmsTestPlan tp SET tp.milestone = null "
      + "WHERE tp.id = :testPlanId AND tp.milestone.id = :milestoneId "
      + "AND tp.project.id = :projectId")
  int removeTestPlanFromMilestone(@Param("milestoneId") Long milestoneId,
      @Param("testPlanId") Long testPlanId,
      @Param("projectId") Long projectId);

  /**
   * Adds test plan to milestone by setting milestone_id.
   *
   * @param milestoneId the milestone ID
   * @param testPlanId  the test plan ID
   * @param projectId   the project ID
   * @return number of updated records
   */
  @Modifying
  @Query("UPDATE TmsTestPlan tp SET tp.milestone.id = :milestoneId "
      + "WHERE tp.id = :testPlanId "
      + "AND tp.project.id = :projectId")
  int addTestPlanToMilestone(@Param("milestoneId") Long milestoneId,
      @Param("testPlanId") Long testPlanId,
      @Param("projectId") Long projectId);

  @Modifying
  @Query(value = "UPDATE tms_test_plan SET milestone_id = NULL "
      + "WHERE milestone_id = :milestoneId AND project_id = :projectId",
      nativeQuery = true)
  void removeTestPlansFromMilestone(@Param("milestoneId") Long milestoneId,
      @Param("projectId") Long projectId);

  @Query(value = "SELECT id, name FROM tms_test_plan " +
      "WHERE project_id = :projectId " +
      "AND (:search IS NULL OR name ILIKE '%' || CAST(:search AS varchar) || '%' OR display_id ILIKE '%' || CAST(:search AS varchar) || '%')",
      countQuery = "SELECT COUNT(id) FROM tms_test_plan " +
      "WHERE project_id = :projectId " +
      "AND (:search IS NULL OR name ILIKE '%' || CAST(:search AS varchar) || '%' OR display_id ILIKE '%' || CAST(:search AS varchar) || '%')",
      nativeQuery = true)
  Page<TmsTestPlanName> findIdAndNameByProjectIdAndSearch(
      @Param("projectId") Long projectId,
      @Param("search") String search,
      Pageable pageable);
}
