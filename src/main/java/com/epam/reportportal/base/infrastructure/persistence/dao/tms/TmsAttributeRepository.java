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

  @Query(value = "SELECT DISTINCT key FROM tms_attribute " +
      "WHERE project_id = :projectId " +
      "AND (:search IS NULL OR key ILIKE '%' || :search || '%')",
      nativeQuery = true)
  List<String> findDistinctKeysByProjectIdAndKeyLike(
      @Param("projectId") Long projectId,
      @Param("search") String search);

  @Query(value = "SELECT DISTINCT key FROM tms_attribute " +
      "WHERE project_id = :projectId " +
      "AND (:search IS NULL OR value ILIKE '%' || :search || '%')",
      nativeQuery = true)
  List<String> findDistinctValuesByProjectIdAndValueLike(
      @Param("projectId") Long projectId,
      @Param("search") String search);

  @Query(
      "SELECT a FROM TmsAttribute a "
          + "WHERE a.project.id = :projectId "
          + "AND a.id IN ("
          +   "SELECT tca.attribute.id FROM TmsTestCaseAttribute tca "
          +   "WHERE tca.testCase.id IN :testCaseIds "
          +   "GROUP BY tca.attribute.id "
          +   "HAVING COUNT(DISTINCT tca.testCase.id) = :#{#testCaseIds.size()}"
          + ")")
  Page<TmsAttribute> findDistinctByTestCaseIdsAndProjectId(
      @Param("projectId") Long projectId,
      @Param("testCaseIds") List<Long> testCaseIds,
      Pageable pageable);

  boolean existsByKey(String key);
}
