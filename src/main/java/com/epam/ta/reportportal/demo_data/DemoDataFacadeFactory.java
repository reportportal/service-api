package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;

/**
 * Created by Pavel_Bortnik on 4/27/2017.
 */
public interface DemoDataFacadeFactory {
    DemoDataFacade getDemoDataFacade(StatisticsCalculationStrategy strategy);
}
