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

import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static java.util.stream.Collectors.toList;

@Service
public class DemoDataFacadeImplBdd extends DemoDataCommon implements DemoDataFacade {

    private static final String BDD_NAME = "BDD Demo Api Tests";

    private static final StatisticsCalculationStrategy strategy = StatisticsCalculationStrategy.TEST_BASED;

    @Value("classpath:demo/demo_bdd.json")
    private Resource resource;

    @Override
    public List<String> generateDemoLaunches(DemoDataRq rq, String user, String projectName) {
        Map<String, Map<String, List<String>>> stories;
        try {
            stories = objectMapper.readValue(resource.getURL(), new TypeReference<Map<String, Map<String, List<String>>>>() {
            });
        } catch (IOException e) {
            throw new ReportPortalException("Unable to load stories description. " + e.getMessage(), e);
        }
        return generateLaunches(rq, stories, user, projectName);
    }

    private List<String> generateLaunches(DemoDataRq rq, Map<String, Map<String, List<String>>> storiesStructure, String user,
                                          String project) {
        return IntStream.range(0, rq.getLaunchesQuantity()).mapToObj(i -> {
            String launchId = startLaunch(BDD_NAME + "_" + rq.getPostfix(), i, project, user);
            generateStories(storiesStructure, i, launchId);
            finishLaunch(launchId);
            return launchId;
        }).collect(toList());
    }

    private List<String> generateStories(Map<String, Map<String, List<String>>> storiesStructure, int i, String launchId){
        String customStory = generateCustomStory(launchId);
        List<String> list = storiesStructure.entrySet().stream().limit(i + 1).map(story ->{
            TestItem storyItem = startRootItem(story.getKey(), launchId, STORY);
            story.getValue().entrySet().forEach(scenario -> {
                TestItem scenarioItem = startTestItem(storyItem, launchId, scenario.getKey(), SCENARIO);
                boolean isFailed = false;
                for(String step: scenario.getValue()){
                    TestItem stepItem = startTestItem(scenarioItem, launchId, step, STEP);
                    String status = status();
                    if (isFailed){
                        status = SKIPPED.name();
                    }
                    if (FAILED.name().equalsIgnoreCase(status) || SKIPPED.name().equalsIgnoreCase(status)){
                        isFailed = true;
                    }
                    logDemoDataService.generateDemoLogs(stepItem.getId(), status);
                    finishTestItem(stepItem.getId(), status, strategy);
                }
                if (isFailed){
                    finishTestItem(scenarioItem.getId(), FAILED.name(), strategy);
                }else {
                    finishTestItem(scenarioItem.getId(), PASSED.name(), strategy);
                }
            });
            finishRootItem(storyItem.getId());
            return storyItem.getId();
        }).collect(toList());
        list.add(customStory);
        return list;
    }

    String generateCustomStory(String launchId){
        TestItem outerStory = startRootItem("Custom story", launchId, STORY);
        TestItem innerStory = startTestItem(outerStory,launchId,"Inner Story", STORY);
        TestItem innerScenario = startTestItem(innerStory, launchId, "Inner Scenario", SCENARIO);
        TestItem innerStep = startTestItem(innerScenario, launchId, "Inner Step", STEP);
        TestItem outerScenario = startTestItem(outerStory, launchId, "Simple Scenario", SCENARIO);
        TestItem outerStep = startTestItem(outerScenario, launchId, "Simple Step", STEP);
        logDemoDataService.generateDemoLogs(innerStep.getId(), FAILED.name());
        logDemoDataService.generateDemoLogs(outerStep.getId(), PASSED.name());
        finishTestItem(outerStep.getId(), PASSED.name(), strategy);
        finishTestItem(outerScenario.getId(), PASSED.name(), strategy);
        finishTestItem(innerStep.getId(), PASSED.name(), strategy);
        finishTestItem(innerScenario.getId(), FAILED.name(), strategy);
        finishTestItem(innerStory.getId(), PASSED.name(), strategy);
        finishRootItem(outerStory.getId());
        return outerStory.getId();
    }
}
