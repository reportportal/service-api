package com.epam.ta.reportportal.store.database.dao;

import com.epam.ta.reportportal.store.database.entity.integration.Integration;

/**
 * Repository for {@link com.epam.ta.reportportal.store.database.entity.integration.Integration} entity
 *
 * @author Yauheni_Martynau
 */
public interface IntegrationRepository extends ReportPortalRepository<Integration, Long>, IntegrationRepositoryCustom {
}
