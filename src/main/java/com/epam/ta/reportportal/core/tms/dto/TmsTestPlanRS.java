package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanRS {

    private Long id;
    private String name;
    private String description;
    private TmsTestPlanEnvironmentRS environment;
    private TmsTestPlanProductVersionRS productVersion;
    private List<TmsTestPlanAttributeRS> attributes;
    private List<TmsTestPlanMilestoneRS> milestones;
}
