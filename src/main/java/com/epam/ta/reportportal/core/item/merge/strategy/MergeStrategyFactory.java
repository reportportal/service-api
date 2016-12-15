package com.epam.ta.reportportal.core.item.merge.strategy;

import java.util.Map;

public class MergeStrategyFactory {
    private Map<MergeStrategyType, MergeStrategy> mapping;

    public MergeStrategyFactory(Map<MergeStrategyType, MergeStrategy> mapping) {
        this.mapping = mapping;
    }

    public MergeStrategy getStrategy(MergeStrategyType type) {
        return this.mapping.get(type);
    }
}
