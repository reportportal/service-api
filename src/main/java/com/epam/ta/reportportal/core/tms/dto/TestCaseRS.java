package com.epam.ta.reportportal.core.tms.dto;

import java.util.Set;

public record TestCaseRS(long id, String name, String description, Set<TestCaseVersionRS> testCaseVersions) {
    
}
