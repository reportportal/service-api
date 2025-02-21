package com.epam.ta.reportportal.core.tms.dto;

public record TestCaseVersionRS(long id,
                                String name,
                                boolean isDefault,
                                boolean isDraft,
                                ManualScenarioRS manualScenario) {
    
}
