package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import java.util.List;

public interface TmsTestCaseService extends CrudService<TmsTestCaseRQ, TmsTestCaseRS, Long> {

  List<TmsTestCaseRS> getTestCaseByProjectId(long projectId);
}
