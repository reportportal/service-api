package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static java.util.stream.Collectors.toList;

@Service
public class DemoDataFacadeImpl extends DemoDataCommon implements DemoDataFacade {

    private static final String NAME = "Demo Api Tests";

    private static final StatisticsCalculationStrategy strategy = StatisticsCalculationStrategy.STEP_BASED;

    @Value("classpath:demo/demo_data.json")
    private Resource resource;

    @Override
    public List<String> generateDemoLaunches(DemoDataRq demoDataRq, String user, String projectName) {
        Map<String, Map<String, List<String>>> suites;
        try {
            suites = objectMapper.readValue(resource.getURL(), new TypeReference<Map<String, Map<String, List<String>>>>() {
            });
        } catch (IOException e) {
            throw new ReportPortalException("Unable to load suites description. " + e.getMessage(), e);
        }
        return generateLaunches(demoDataRq, suites, user, projectName, StatisticsCalculationStrategy.STEP_BASED);
    }

    private List<String> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> suitesStructure, String user,
                                          String project, StatisticsCalculationStrategy statsStrategy) {
        return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
            String launchId = startLaunch(NAME + "_" + rq.getPostfix(), i, project, user);
            generateSuites(suitesStructure, i, launchId, statsStrategy);
            finishLaunch(launchId);
            return launchId;
        }).collect(toList());
    }

    private List<String> generateSuites(Map<String, Map<String, List<String>>> suitesStructure, int i, String launchId, StatisticsCalculationStrategy statsStrategy) {
        return suitesStructure.entrySet().stream().limit(i + 1).map(suites -> {
            TestItem suiteItem = startRootItem(suites.getKey(), launchId, SUITE);
            suites.getValue().entrySet().forEach(tests -> {
                TestItem testItem = startTestItem(suiteItem, launchId, tests.getKey(), TEST);
                String beforeClassStatus = "";
                if (random.nextBoolean()) {
                    TestItem beforeClass = startTestItem(testItem, launchId, "beforeClass", BEFORE_CLASS);
                    beforeClassStatus = beforeClassStatus();
                    finishTestItem(beforeClass.getId(), beforeClassStatus, statsStrategy);
                }
                boolean isGenerateBeforeMethod = random.nextBoolean();
                boolean isGenerateAfterMethod = random.nextBoolean();
                tests.getValue().stream().limit(i + 1).forEach(name -> {
                    if (isGenerateBeforeMethod) {
                        finishTestItem(
                                startTestItem(testItem, launchId, "beforeMethod", BEFORE_METHOD).getId(), status(), statsStrategy);
                    }
                    TestItem stepId = startTestItem(testItem, launchId, name, STEP);
                    String status = status();
                    logDemoDataService.generateDemoLogs(stepId.getId(), status);
                    finishTestItem(stepId.getId(), status, statsStrategy);
                    if (isGenerateAfterMethod) {
                        finishTestItem(
                                startTestItem(testItem, launchId, "afterMethod", AFTER_METHOD).getId(), status(), statsStrategy);
                    }
                });
                if (random.nextBoolean()) {
                    TestItem afterClass = startTestItem(testItem, launchId, "afterClass", AFTER_CLASS);
                    finishTestItem(afterClass.getId(), status(), statsStrategy);
                }
                finishTestItem(testItem.getId(), !beforeClassStatus.isEmpty() ? beforeClassStatus : "FAILED", statsStrategy);
            });
            finishRootItem(suiteItem.getId());
            return suiteItem.getId();
        }).collect(toList());
    }
}
