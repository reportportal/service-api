package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlanAttributeId;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsTestPlanAttributeRepository
    extends ReportPortalRepository<TmsTestPlanAttribute, TmsTestPlanAttributeId> {

    @Modifying
    void deleteAllById_TestPlanId(Long testPlanId);
}
