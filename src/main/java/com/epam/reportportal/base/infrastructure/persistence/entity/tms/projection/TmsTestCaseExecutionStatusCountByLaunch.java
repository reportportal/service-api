package com.epam.reportportal.base.infrastructure.persistence.entity.tms.projection;

/**
 * Projection for aggregated execution counts by status.
 */
public interface TmsTestCaseExecutionStatusCountByLaunch {

  String getStatus();

  Long getCount();

  Long getLaunchId();
}
