package com.epam.reportportal.infrastructure.persistence.entity.tms.projection;

/**
 * Projection for aggregated execution counts by status.
 */
public interface TmsTestCaseExecutionStatusCountByLaunch {

  String getStatus();

  Long getCount();

  Long getLaunchId();
}
