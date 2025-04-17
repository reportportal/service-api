package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttribute;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseAttributeId;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface TmsTestCaseAttributeRepository extends
    ReportPortalRepository<TmsTestCaseAttribute, TmsTestCaseAttributeId> {

  @Modifying
  void deleteAllById_TestCaseId(Long testCaseId);

}
