package com.epam.ta.reportportal.core.tms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TestCaseRQ(@NotEmpty String name, String description, @NotNull Long testFolderId, List<AttributeRQ> attributes) {
    
}
