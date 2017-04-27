package com.epam.ta.reportportal.demo_data;

import java.util.List;

/**
 * Created by Pavel_Bortnik on 4/27/2017.
 */
public interface DemoDataFacade {
    List<String> generateDemoLaunches(DemoDataRq demoDataRq, String user, String projectName);
}
