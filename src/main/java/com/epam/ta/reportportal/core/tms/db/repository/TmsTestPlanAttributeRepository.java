package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttributeId;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestPlanAttributeRepository
    extends ReportPortalRepository<TmsTestPlanAttribute, TmsTestPlanAttributeId> {

  @Modifying
  @Query(value = "DELETE FROM TmsTestPlanAttribute tpa "
      + "WHERE tpa.id.testPlanId = :testPlanId"
  )
  void deleteAllByTestPlanId(@Param("testPlanId") Long testPlanId);
}
