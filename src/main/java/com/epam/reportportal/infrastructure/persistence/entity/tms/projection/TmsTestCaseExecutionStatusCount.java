package com.epam.reportportal.infrastructure.persistence.entity.tms.projection;

/**
 * Projection for aggregated execution counts by status.
 */
public interface TmsTestCaseExecutionStatusCount {

  String getStatus();

  Long getCount();
}
