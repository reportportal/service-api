package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
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

  Boolean existsByIdAndProject_Id(Long testPlanId, Long projectId);
}
