package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DemoDataFacadeFactoryImpl implements DemoDataFacadeFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static final Map<StatisticsCalculationStrategy, Class<? extends DemoDataFacade>> MAPPING =
            ImmutableMap.<StatisticsCalculationStrategy, Class<? extends DemoDataFacade>>builder()
                    .put(StatisticsCalculationStrategy.STEP_BASED, StepBasedDemoDataFacade.class)
                    .put(StatisticsCalculationStrategy.TEST_BASED, TestBasedDemoDataFacade.class)
                    .build();

    @Override
    public DemoDataFacade getDemoDataFacade(StatisticsCalculationStrategy strategy) {
        return applicationContext.getBean(MAPPING.getOrDefault(strategy, StepBasedDemoDataFacade.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
