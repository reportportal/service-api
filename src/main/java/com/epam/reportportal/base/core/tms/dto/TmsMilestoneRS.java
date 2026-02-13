package com.epam.reportportal.base.core.tms.dto;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
public class TmsMilestoneRS {

  private Long id;

  private String name;

  private TmsMilestoneType type;

  private TmsMilestoneStatus status;

  private Instant startDate;

  private Instant endDate;

  private List<TmsTestPlanRS> testPlans;
}
