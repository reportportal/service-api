/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.demodata;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.GetTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.core.item.impl.TestItemUniqueIdGenerator;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.UUID;

import static com.epam.ta.reportportal.core.statistics.StatisticsHelper.getStatusFromStatistics;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.*;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.LAUNCH_NOT_FOUND;

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
	protected StartLaunchHandler startLaunchHandler;

	@Autowired
	protected FinishLaunchHandler finishLaunchHandler;

	@Autowired
	protected StartTestItemHandler startTestItemHandler;

	@Autowired
	protected FinishTestItemHandler finishTestItemHandler;

	@Autowired
	protected GetTestItemHandler getTestItemHandler;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected TestItemRepository testItemRepository;

	@Autowired
	protected IssueTypeHandler issueTypeHandler;

	@Autowired
	protected UserRepository userRepository;

	Long startLaunch(String name, int i, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setMode(Mode.DEFAULT);
		rq.setDescription(ContentUtils.getLaunchDescription());
		rq.setName(name);
		rq.setStartTime(EntityUtils.TO_DATE.apply(LocalDateTime.now()));
		rq.setTags(ImmutableSet.<String>builder().addAll(Arrays.asList("desktop", "demo", "build:3.0.1." + (i + 1))).build());
		rq.setUuid(String.valueOf(UUID.randomUUID()));

		return startLaunchHandler.startLaunch(user, projectDetails, rq).getId();
	}

	void finishLaunch(Long launchId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		//Tte same in finishLaunch()
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));

		FinishExecutionRQ rq = new FinishExecutionRQ();
		rq.setEndTime(EntityUtils.TO_DATE.apply(LocalDateTime.now()));
		rq.setStatus(getStatusFromStatistics(launch.getStatistics()).getExecutionCounterField());

		finishLaunchHandler.finishLaunch(launchId, rq, projectDetails, user);
	}

	Long startRootItem(String rootItemName, Long launchId, TestItemTypeEnum type, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(rootItemName);
		rq.setLaunchId(launchId);
		rq.setStartTime(EntityUtils.TO_DATE.apply(LocalDateTime.now()));
		rq.setType(type.name());
		rq.setUuid(String.valueOf(UUID.randomUUID()));
		if (type.sameLevel(SUITE) && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			rq.setTags(ContentUtils.getTagsInRange(TAGS_COUNT));
			rq.setDescription(ContentUtils.getSuiteDescription());
		}

		return startTestItemHandler.startRootItem(user, projectDetails, rq).getId();
	}

	void finishRootItem(Long rootItemId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(EntityUtils.TO_DATE.apply(LocalDateTime.now()));
		//finishExecutionRQ.setStatus(getStatusFromStatistics(itemResults.getStatistics()));

		finishTestItemHandler.finishTestItem(user, projectDetails, rootItemId, rq);
	}

	Long startTestItem(Long rootItemId, Long launchId, String name, TestItemTypeEnum type, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		StartTestItemRQ rq = new StartTestItemRQ();
		if (ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			if (hasChildren(type)) {
				rq.setTags(ContentUtils.getTagsInRange(TAGS_COUNT));
				rq.setDescription(ContentUtils.getTestDescription());
			} else {
				rq.setTags(ContentUtils.getTagsInRange(TAGS_COUNT));
				rq.setDescription(ContentUtils.getStepDescription());
			}
		}
		rq.setLaunchId(launchId);
		rq.setStartTime(EntityUtils.TO_DATE.apply(LocalDateTime.now()));
		rq.setName(name);

		return startTestItemHandler.startChildItem(user, projectDetails, rq, rootItemId).getId();
	}

	@Transactional
	void finishTestItem(Long testItemId, String status, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(EntityUtils.TO_DATE.apply(LocalDateTime.now()));
		rq.setStatus(status);
		if ("FAILED".equals(status)) {
			rq.setIssue(issueType());
		}
		finishTestItemHandler.finishTestItem(user, projectDetails, testItemId, rq);
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

	boolean hasChildren(TestItemTypeEnum testItemType) {
		return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
				|| testItemType == AFTER_METHOD);
	}

	Issue issueType() {
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
