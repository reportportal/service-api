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

import com.epam.ta.reportportal.core.item.TestItemUniqueIdGenerator;
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

	static final int STORY_PROBABILITY = 30;

	protected SplittableRandom random = new SplittableRandom();

	static final int CONTENT_PROBABILITY = 60;

	private static final int TAGS_COUNT = 3;

	@Autowired
	DemoLogsService logDemoDataService;

	@Autowired
	private TestItemUniqueIdGenerator identifierGenerator;

	@Autowired
	protected LaunchRepository launchRepository;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected TestItemRepository testItemRepository;

	@Autowired
	protected StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private LaunchMetaInfoRepository launchCounter;

	String startLaunch(String name, int i, String project, String user) {
		Launch launch = new Launch();
		launch.setName(name);
		launch.setStartTime(new Date());
		launch.setTags(ImmutableSet.<String>builder().addAll(Arrays.asList("desktop", "demo", "build:3.0.1." + (i + 1))).build());
		launch.setDescription(ContentUtils.getLaunchDescription());
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

	TestItem startRootItem(String rootItemName, String launchId, TestItemType type, String project) {
		TestItem testItem = new TestItem();
		testItem.setLaunchRef(launchId);
		if (type.sameLevel(SUITE) && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			testItem.setTags(ContentUtils.getTagsInRange(TAGS_COUNT));
			testItem.setItemDescription(ContentUtils.getSuiteDescription());
		}
		testItem.setStartTime(new Date());
		testItem.setName(rootItemName);
		testItem.setHasChilds(true);
		testItem.setStatus(IN_PROGRESS);
		testItem.setType(type);
		testItem.setUniqueId(identifierGenerator.generate(testItem));
		return testItemRepository.save(testItem);
	}

	void finishRootItem(String rootItemId) {
		TestItem testItem = testItemRepository.findOne(rootItemId);
		testItem.setEndTime(new Date());
		testItem.setStatus(getStatusFromStatistics(testItem.getStatistics()));
		testItemRepository.save(testItem);
	}

	TestItem startTestItem(TestItem rootItemId, String launchId, String name, TestItemType type, String project) {
		TestItem testItem = new TestItem();
		if (ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			if (hasChildren(type)) {
				testItem.setTags(ContentUtils.getTagsInRange(TAGS_COUNT));
				testItem.setItemDescription(ContentUtils.getTestDescription());
			} else {
				testItem.setTags(ContentUtils.getTagsInRange(TAGS_COUNT));
				testItem.setItemDescription(ContentUtils.getStepDescription());
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
		testItem.setUniqueId(identifierGenerator.generate(testItem));
		return testItemRepository.save(testItem);
	}

	void finishTestItem(String testItemId, String status, StatisticsCalculationStrategy statsStrategy) {
		TestItem testItem = testItemRepository.findOne(testItemId);
		StatisticsFacade statisticsFacade = statisticsFacadeFactory.getStatisticsFacade(statsStrategy);
		if ("FAILED".equals(status) && statisticsFacade.awareIssue(testItem)) {
			testItem.setIssue(issueType());
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
		if (ContentUtils.getWithProbability(STATUS_PROBABILITY)) {
			return SKIPPED.name();
		} else if (ContentUtils.getWithProbability(2 * STATUS_PROBABILITY)) {
			return FAILED.name();
		}
		return PASSED.name();
	}

	boolean hasChildren(TestItemType testItemType) {
		return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
				|| testItemType == AFTER_METHOD);
	}

	TestItemIssue issueType() {
		int ISSUE_PROBABILITY = 25;
		if (ContentUtils.getWithProbability(ISSUE_PROBABILITY)) {
			return ContentUtils.getProductBug();
		} else if (ContentUtils.getWithProbability(ISSUE_PROBABILITY)) {
			return ContentUtils.getAutomationBug();
		} else if (ContentUtils.getWithProbability(ISSUE_PROBABILITY)) {
			return ContentUtils.getSystemIssue();
		} else {
			return ContentUtils.getInvestigate();
		}
	}

}
