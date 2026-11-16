package com.epam.reportportal.base.infrastructure.persistence.entity.tms;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanExecutionStatistic {

  private Long covered;
  private Long total;

  public TmsTestPlanExecutionStatistic(Number total, Number covered) {
    this.total = total != null ? total.longValue() : 0L;
    this.covered = covered != null ? covered.longValue() : 0L;
  }
}
