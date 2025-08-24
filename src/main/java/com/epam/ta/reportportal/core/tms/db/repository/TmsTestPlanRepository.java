package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
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

  @Query("SELECT tp FROM TmsTestPlan tp " +
      "LEFT JOIN FETCH tp.attributes atr " +
      "LEFT JOIN FETCH atr.attribute " +
      "WHERE tp.project.id = :projectId"
  )
  Page<TmsTestPlan> findByCriteria(
      @Param("projectId") Long projectId,
      Pageable pageable);

  Boolean existsByIdAndProject_Id(Long testPlanId, Long projectId);
}
