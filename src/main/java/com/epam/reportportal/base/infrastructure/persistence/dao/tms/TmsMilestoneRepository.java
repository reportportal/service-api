package com.epam.reportportal.base.infrastructure.persistence.dao.tms;

import com.epam.reportportal.base.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsMilestoneRepository extends ReportPortalRepository<TmsMilestone, Long> {

  @Modifying
  @Query("UPDATE TmsMilestone m SET m.testPlan = :testPlan WHERE m.id = :milestoneId")
  void attachTestPlanToMilestone(@Param("testPlan") TmsTestPlan testPlan,
      @Param("milestoneId") Long milestoneId);

  @Modifying
  @Query("UPDATE TmsMilestone m SET m.testPlan = null WHERE m.testPlan.id = :testPlanId")
  void detachTestPlanFromMilestones(@Param("testPlanId") Long testPlanId);
}
