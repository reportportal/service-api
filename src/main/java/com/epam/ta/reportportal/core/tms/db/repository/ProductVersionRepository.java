package com.epam.ta.reportportal.core.tms.db.repository;

import com.epam.ta.reportportal.core.tms.db.model.ProductVersion;
import com.epam.ta.reportportal.dao.ReportPortalRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVersionRepository extends ReportPortalRepository<ProductVersion, Long> {
    
}
