package com.epam.reportportal.infrastructure.persistence.dao.tms;

import com.epam.reportportal.infrastructure.persistence.dao.ReportPortalRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsAttribute;
import org.springframework.stereotype.Repository;

@Repository
public interface TmsAttributeRepository extends ReportPortalRepository<TmsAttribute, Long> {

  boolean existsByKey(String key);
}
