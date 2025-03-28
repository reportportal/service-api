package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseAttributeRQ;
import java.util.List;

public interface TmsTestCaseAttributeService {

    void createTestCaseAttributes(TmsTestCase tmsTestCase, List<TmsTestCaseAttributeRQ> attributes);

    void updateTestCaseAttributes(TmsTestCase tmsTestCase, List<TmsTestCaseAttributeRQ> attributes);

    void patchTestCaseAttributes(TmsTestCase tmsTestCase, List<TmsTestCaseAttributeRQ> attributes);

    void deleteAllByTestCaseId(Long testCaseId);
}
