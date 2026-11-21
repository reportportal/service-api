package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Execution statistics of a test plan. Create it with {@link #builder()} only, so total and
 * covered are always set by name.
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanExecutionStatistic {

  private Long covered;
  private Long total;
}
