package com.epam.ta.reportportal.core.imprt.format.async;

import java.util.Arrays;

public enum ImportType {
    JUNIT;

    public static ImportType fromValue(String value) {
        return Arrays.stream(ImportType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst().orElse(null);
    }
}
