package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
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
      "LEFT JOIN FETCH tp.environment e " +
      "LEFT JOIN FETCH tp.productVersion pv " +
      "LEFT JOIN FETCH tp.milestones ms " +
      "LEFT JOIN FETCH ms.productVersion mspv " +
      "LEFT JOIN FETCH tp.attributes atr " +
      "LEFT JOIN FETCH atr.attribute " +
      "WHERE tp.project.id = :projectId AND tp.id = :id"
  )
  Optional<TmsTestPlan> findByIdAndProjectId(Long id, Long projectId);

  @Modifying
  void deleteByIdAndProject_Id(Long id, Long projectId);

  @Query("SELECT tp FROM TmsTestPlan tp " +
      "LEFT JOIN FETCH tp.environment e " +
      "LEFT JOIN FETCH tp.productVersion pv " +
      "LEFT JOIN FETCH tp.milestones ms " +
      "LEFT JOIN FETCH ms.productVersion mspv " +
      "LEFT JOIN FETCH tp.attributes atr " +
      "LEFT JOIN FETCH atr.attribute " +
      "WHERE tp.project.id = :projectId AND" +
      "(:environmentIds IS NULL OR tp.environment.id IN :environmentIds) AND " +
      "(:productVersionIds IS NULL OR tp.productVersion.id IN :productVersionIds)"
  )
  Page<TmsTestPlan> findByCriteria(
      @Param("projectId") Long projectId,
      @Param("environmentIds") List<Long> environmentIds,
      @Param("productVersionIds") List<Long> productVersionIds,
      Pageable pageable);
}
