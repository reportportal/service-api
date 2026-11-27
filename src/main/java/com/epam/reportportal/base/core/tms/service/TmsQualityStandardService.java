package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;

public interface TmsQualityStandardService {

  TmsQualityStandardRS getStandard(Long projectId);

  TmsQualityStandardRS createStandard(Long projectId, TmsQualityStandardRQ rq);

  TmsQualityStandardRS updateStandard(Long projectId, TmsQualityStandardRQ rq);

  void deleteStandard(Long projectId);
}
