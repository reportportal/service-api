package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TmsTestPlanRQ {

    private String name;
    private String description;
    private Long environmentId;
    private Long productVersionId;
    private List<TmsTestPlanAttributeRQ> attributes;
    private List<Long> milestoneIds;
}
