package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseQualityScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseQualityScoreRepository extends JpaRepository<TmsTestCaseQualityScore, Long> {

  List<TmsTestCaseQualityScore> findByTestCaseVersionId(Long testCaseVersionId);

  void deleteByTestCaseVersionId(Long testCaseVersionId);
}
