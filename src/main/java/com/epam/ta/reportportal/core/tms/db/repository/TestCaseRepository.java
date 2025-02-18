package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.model.TestCase;
import com.epam.ta.reportportal.dao.ReportPortalRepository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepository extends ReportPortalRepository<TestCase, Long> {
    List<TestCase> findByTestFolder_ProjectId(long projectId);
}
