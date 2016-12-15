package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.item.merge.strategy.*;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MergeStrategyConfig {
    @Autowired
    private TestItemRepository testItemRepository;

    @Bean
    public Map<MergeStrategyType, MergeStrategy> mapping() {
        Map<MergeStrategyType, MergeStrategy> mapping = new HashMap<>();
        mapping.put(MergeStrategyType.TEST, new SuiteMergeStrategy(testItemRepository));
        mapping.put(MergeStrategyType.SUITE, new TestMergeStrategy(testItemRepository));
        return mapping;
    }

    @Bean
    public MergeStrategyFactory factory() {
        return new MergeStrategyFactory(mapping());
    }
}
