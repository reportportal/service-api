package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestCaseRepository extends ReportPortalRepository<TmsTestCase, Long> {

  @Query("SELECT tc FROM TmsTestCase tc " +
      "JOIN FETCH tc.testFolder tf " +
      "LEFT JOIN FETCH tc.dataset ds " +
      "LEFT JOIN FETCH tc.tags t " +
      "LEFT JOIN FETCH tc.versions v " +
      "WHERE tf.projectId = :projectId"
  )
  List<TmsTestCase> findByTestFolder_ProjectId(Long projectId);

  @Query("SELECT tc FROM TmsTestCase tc " +
      "JOIN FETCH tc.testFolder tf " +
      "LEFT JOIN FETCH tc.dataset ds " +
      "LEFT JOIN FETCH tc.tags t " +
      "LEFT JOIN FETCH tc.versions v " +
      "WHERE tf.projectId = :projectId AND tc.id = :id"
  )
  Optional<TmsTestCase> findByIdAndProjectId(Long id, Long projectId);
}
