package com.epam.ta.reportportal.core.item.merge.strategy;


public enum MergeStrategyType {
    SUITE,
    TEST;

    public static MergeStrategyType fromValue(String value) {
        MergeStrategyType[] values = MergeStrategyType.values();
        for (MergeStrategyType type : values) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
