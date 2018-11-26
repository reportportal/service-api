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
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.ItemAttributeResource;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

import static com.epam.ta.reportportal.demodata.Constants.CONTENT_PROBABILITY;
import static com.epam.ta.reportportal.demodata.Constants.TAGS_COUNT;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;

/**
 * @author Pavel_Bortnik
 */
@Service
public class DemoDataCommonService {

	private final StartLaunchHandler startLaunchHandler;

	private final FinishLaunchHandler finishLaunchHandler;

	private final StartTestItemHandler startTestItemHandler;

	private final FinishTestItemHandler finishTestItemHandler;

	@Autowired
	public DemoDataCommonService(StartLaunchHandler startLaunchHandler, FinishLaunchHandler finishLaunchHandler,
			StartTestItemHandler startTestItemHandler, FinishTestItemHandler finishTestItemHandler) {
		this.startLaunchHandler = startLaunchHandler;
		this.finishLaunchHandler = finishLaunchHandler;
		this.startTestItemHandler = startTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
	}

	@Transactional
	public Long startLaunch(String name, int i, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		StartLaunchRQ rq = new StartLaunchRQ();
		rq.setMode(Mode.DEFAULT);
		rq.setDescription(ContentUtils.getLaunchDescription());
		rq.setName(name);
		rq.setStartTime(new Date());
		Set<ItemAttributeResource> tags = Sets.newHashSet(
				new ItemAttributeResource("desktop", "", false),
				new ItemAttributeResource("demo", "", false),
				new ItemAttributeResource("build", "3.0.1", false)
		);
		rq.setAttributes(tags);

		return startLaunchHandler.startLaunch(user, projectDetails, rq).getId();
	}

	@Transactional
	public void finishLaunch(Long launchId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishExecutionRQ rq = new FinishExecutionRQ();
		rq.setEndTime(new Date());

		finishLaunchHandler.finishLaunch(launchId, rq, projectDetails, user);
	}

	@Transactional
	public Long startRootItem(String rootItemName, Long launchId, TestItemTypeEnum type, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(rootItemName);
		rq.setLaunchId(launchId);
		rq.setStartTime(new Date());
		rq.setType(type.name());
		if (type.sameLevel(SUITE) && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			rq.setAttributes(ContentUtils.getTagsInRange(TAGS_COUNT));
			rq.setDescription(ContentUtils.getSuiteDescription());
		}

		return startTestItemHandler.startRootItem(user, projectDetails, rq).getId();
	}

	@Transactional
	public void finishRootItem(Long rootItemId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(new Date());

		finishTestItemHandler.finishTestItem(user, projectDetails, rootItemId, rq);
	}

	@Transactional
	public Long startTestItem(Long rootItemId, Long launchId, String name, TestItemTypeEnum type, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		StartTestItemRQ rq = new StartTestItemRQ();
		if (ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			if (hasChildren(type)) {
				rq.setAttributes(ContentUtils.getTagsInRange(TAGS_COUNT));
				rq.setDescription(ContentUtils.getTestDescription());
			} else {
				rq.setAttributes(ContentUtils.getTagsInRange(TAGS_COUNT));
				rq.setDescription(ContentUtils.getStepDescription());
			}
		}
		rq.setLaunchId(launchId);
		rq.setStartTime(new Date());
		rq.setName(name);
		rq.setType(type.name());

		return startTestItemHandler.startChildItem(user, projectDetails, rq, rootItemId).getId();
	}

	@Transactional
	public void finishTestItem(Long testItemId, String status, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(new Date());
		rq.setStatus(status);
		if ("FAILED".equals(status)) {
			rq.setIssue(issueType());
		}
		finishTestItemHandler.finishTestItem(user, projectDetails, testItemId, rq);
	}

	private boolean hasChildren(TestItemTypeEnum testItemType) {
		return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
				|| testItemType == AFTER_METHOD);
	}

	private Issue issueType() {
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
