package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseQualityScore;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseQualityScoreRepository extends JpaRepository<TmsTestCaseQualityScore, Long> {

  List<TmsTestCaseQualityScore> findByTestCaseVersionId(Long testCaseVersionId);

  @Query("select s from TmsTestCaseQualityScore s join fetch s.criterion "
      + "where s.testCaseVersion.id in :testCaseVersionIds")
  List<TmsTestCaseQualityScore> findByTestCaseVersionIdIn(@Param("testCaseVersionIds") Collection<Long> testCaseVersionIds);

  void deleteByTestCaseVersionId(Long testCaseVersionId);
}
