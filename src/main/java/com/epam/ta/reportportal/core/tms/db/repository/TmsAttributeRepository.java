package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.entity.TmsAttribute;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsAttributeRepository extends ReportPortalRepository<TmsAttribute, Long> {

  boolean existsByKey(String key);
}
