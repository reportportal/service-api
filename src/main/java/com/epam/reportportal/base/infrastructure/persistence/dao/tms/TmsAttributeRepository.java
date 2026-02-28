package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsAttributeRepository extends ReportPortalRepository<TmsAttribute, Long> {

  List<TmsAttribute> findAllByProject_Id(Long projectId);

  Optional<TmsAttribute> findByIdAndProject_Id(Long id, Long projectId);

  List<TmsAttribute> findAllByProject_IdAndKeyIn(Long projectId, Set<String> keys);

  List<TmsAttribute> findAllByProject_IdAndKeyInAndValueIsNull(Long projectId, Set<String> keys);

  Optional<TmsAttribute> findByProject_IdAndKeyAndValueIsNull(Long projectId, String key);

  @Query("SELECT a FROM TmsAttribute a WHERE a.project.id = :projectId AND a.key = :key AND a.value = :value")
  Optional<TmsAttribute> findByProjectIdAndKeyAndValue(@Param("projectId") Long projectId,
      @Param("key") String key, @Param("value") String value);

  @Query("SELECT COUNT(a) > 0 FROM TmsAttribute a WHERE a.project.id = :projectId AND a.key = :key AND (a.value = :value OR (a.value IS NULL AND :value IS NULL))")
  boolean existsByProjectIdAndKeyAndValue(@Param("projectId") Long projectId,
      @Param("key") String key, @Param("value") String value);

  @Query("SELECT DISTINCT a.key FROM TmsAttribute a " +
      "WHERE a.project.id = :projectId " +
      "AND (:search IS NULL OR a.key LIKE %:search%)")
  List<String> findDistinctKeysByProjectIdAndKeyLike(
      @Param("projectId") Long projectId,
      @Param("search") String search);

  @Query("SELECT DISTINCT a.value FROM TmsAttribute a " +
      "WHERE a.project.id = :projectId " +
      "AND a.value IS NOT NULL " +
      "AND (:search IS NULL OR a.value LIKE %:search%)")
  List<String> findDistinctValuesByProjectIdAndValueLike(
      @Param("projectId") Long projectId,
      @Param("search") String search);

  @Query("SELECT DISTINCT a FROM TmsAttribute a "
      + "JOIN TmsTestCaseAttribute tca ON tca.attribute.id = a.id "
      + "WHERE tca.testCase.id IN :testCaseIds "
      + "AND a.project.id = :projectId")
  Page<TmsAttribute> findDistinctByTestCaseIdsAndProjectId(
      @Param("projectId") Long projectId,
      @Param("testCaseIds") List<Long> testCaseIds,
      Pageable pageable);

}