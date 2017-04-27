package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchMetaInfoRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.Arrays.asList;

@Service
public class DemoDataCommon {

    static final String NAME = "Demo Api Tests";

    protected Random random = new Random();

    @Autowired
    DemoLogsService logDemoDataService;

    @Autowired
    protected LaunchRepository launchRepository;

    @Autowired
    private LaunchMetaInfoRepository launchCounter;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestItemRepository testItemRepository;

    @Autowired
    protected StatisticsFacadeFactory statisticsFacadeFactory;

    String startLaunch(String name, int i, String project, String user) {
        Launch launch = new Launch();
        launch.setName(name);
        launch.setDescription("Demo Launch");
        launch.setStartTime(new Date());
        launch.setTags(new HashSet<>(asList("desktop", "demo", "build:3.0.1." + (i + 1))));
        launch.setStatus(IN_PROGRESS);
        launch.setUserRef(user);
        launch.setProjectRef(project);
        launch.setNumber(launchCounter.getLaunchNumber(name, project));
        launch.setMode(DEFAULT);
        return launchRepository.save(launch).getId();
    }

    void finishLaunch(String launchId) {
        final Launch launch = launchRepository.findOne(launchId);
        launch.setEndTime(new Date());
        launch.setStatus(getStatusFromStatistics(launch.getStatistics()));
        launchRepository.save(launch);
    }

    TestItem startRootItem(String rootItemName, String launchId, TestItemType type) {
        TestItem testItem = new TestItem();
        testItem.setLaunchRef(launchId);
        testItem.setStartTime(new Date());
        testItem.setName(rootItemName);
        testItem.setHasChilds(true);
        testItem.setStatus(IN_PROGRESS);
        testItem.setType(type);
        return testItemRepository.save(testItem);
    }

    void finishRootItem(String rootItemId) {
        TestItem testItem = testItemRepository.findOne(rootItemId);
        testItem.setEndTime(new Date());
        testItem.setStatus(getStatusFromStatistics(testItem.getStatistics()));
        testItemRepository.save(testItem);
    }

    TestItem startTestItem(TestItem rootItemId, String launchId, String name, TestItemType testItemType) {
        TestItem testItem = new TestItem();
        testItem.setLaunchRef(launchId);
        testItem.setStartTime(new Date());
        testItem.setName(name);
        testItem.setParent(rootItemId.getId());
        testItem.setHasChilds(hasChildren(testItemType));
        testItem.setStatus(IN_PROGRESS);
        testItem.setType(testItemType);
        testItem.getPath().addAll(rootItemId.getPath());
        testItem.getPath().add(rootItemId.getId());
        return testItemRepository.save(testItem);
    }

    void finishTestItem(String testItemId, String status, StatisticsCalculationStrategy statsStrategy) {
        TestItem testItem = testItemRepository.findOne(testItemId);
        StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(statsStrategy);
        if ("FAILED".equals(status) && statisticsFacade.awareIssue(testItem)) {
            testItem.setIssue(new TestItemIssue(issueType(), null));
        }
        testItem.setStatus(Status.fromValue(status).get());
        testItem.setEndTime(new Date());
        testItemRepository.save(testItem);
        statisticsFacade.updateExecutionStatistics(testItem);
        if (null != testItem.getIssue()) {
            statisticsFacade.updateIssueStatistics(testItem);
        }
    }

    String status() {
        // magic numbers to generate a distribution of steps statuses
        int value = random.nextInt(71);
        if (value <= 5) {
            return SKIPPED.name();
        } else if (value <= 48) {
            return PASSED.name();
        } else {
            return FAILED.name();
        }
    }

    String beforeClassStatus() {
        // magic numbers to generate a distribution of before/after statuses
        int value = random.nextInt(71);
        if (value <= 3) {
            return SKIPPED.name();
        } else if (value <= 62) {
            return PASSED.name();
        } else {
            return FAILED.name();
        }
    }

    boolean hasChildren(TestItemType testItemType) {
        return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
                || testItemType == AFTER_METHOD);
    }

    String issueType() {
        final int value = random.nextInt(100);
        if (value < 25) {
            return "PB001";
        } else if (value < 50) {
            return "AB001";
        } else if (value < 75) {
            return "SI001";
        } else {
            return "TI001";
        }
    }
}
