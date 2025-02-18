package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.dao.ReportPortalRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends ReportPortalRepository<TmsTestCase, Long> {
    List<TmsTestCase> findByTestFolder_ProjectId(long projectId);
}
