/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.SplittableRandom;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.database.entity.Status.*;
import static com.epam.ta.reportportal.database.entity.item.TestItemType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;

/**
 * @author Pavel_Bortnik
 */
@Service
public class DemoDataCommonService {

    static final String NAME = "Demo Api Tests";

    protected SplittableRandom random = new SplittableRandom();

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

    private ContentUtils contentUtils;

    private static final Range<Integer> PROBABILITY_RANGE = Range.openClosed(0, 100);

    String startLaunch(String name, int i, String project, String user) {
        contentUtils = new ContentUtils();
        contentUtils.initContent();
        Launch launch = new Launch();
        launch.setName(name);
        launch.setStartTime(new Date());
        launch.setTags(ImmutableSet.<String>builder().addAll(Arrays.asList("desktop", "demo",
                "build:3.0.1." + (i + 1))).build());
        launch.setDescription(contentUtils.getLaunchDescription());
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
        contentUtils = null;
    }

    TestItem startRootItem(String rootItemName, String launchId, TestItemType type) {
        TestItem testItem = new TestItem();
        testItem.setLaunchRef(launchId);
        if (type.sameLevel(SUITE) && random.nextBoolean()) {
            testItem.setTags(contentUtils.getTagsInRange(3));
            testItem.setItemDescription(contentUtils.getSuiteDescription());
        }
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

    TestItem startTestItem(TestItem rootItemId, String launchId, String name, TestItemType type) {
        TestItem testItem = new TestItem();
        if (random.nextBoolean()) {
            if (hasChildren(type)) {
                testItem.setTags(contentUtils.getTagsInRange(2));
                testItem.setItemDescription(contentUtils.getTestDescription());
            }else {
                testItem.setTags(contentUtils.getTagsInRange(1));
                testItem.setItemDescription(contentUtils.getStepDescription());
            }
        }
        testItem.setLaunchRef(launchId);
        testItem.setStartTime(new Date());
        testItem.setName(name);
        testItem.setParent(rootItemId.getId());
        testItem.setHasChilds(hasChildren(type));
        testItem.setStatus(IN_PROGRESS);
        testItem.setType(type);
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
        int STATUS_PROBABILITY = 15;
        if (checkProbability(STATUS_PROBABILITY)) {
            return SKIPPED.name();
        } else if (checkProbability(2 * STATUS_PROBABILITY)) {
            return FAILED.name();
        }
        return PASSED.name();
    }

    boolean hasChildren(TestItemType testItemType) {
        return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
                || testItemType == AFTER_METHOD);
    }

    String issueType() {
        int ISSUE_PROBABILITY = 25;
        if (checkProbability(ISSUE_PROBABILITY)) {
            return "PB001";
        } else if (checkProbability(ISSUE_PROBABILITY)) {
            return "AB001";
        } else if (checkProbability(ISSUE_PROBABILITY)) {
            return "SI001";
        } else {
            return "TI001";
        }
    }

    private boolean checkProbability(int probability) {
        return Range.openClosed(PROBABILITY_RANGE.lowerEndpoint(), probability)
                .contains(random.nextInt(PROBABILITY_RANGE.upperEndpoint()));
    }
}
