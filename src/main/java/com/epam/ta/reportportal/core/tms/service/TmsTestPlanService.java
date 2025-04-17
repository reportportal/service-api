package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TmsTestPlanService extends CrudService<TmsTestPlanRQ, TmsTestPlanRS, Long> {

  Page<TmsTestPlanRS> getByCriteria(Long projectId, List<Long> environmentIds,
      List<Long> productVersionIds,
      Pageable pageable);
}
