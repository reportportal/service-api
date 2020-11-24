/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.demodata.model.DemoItemMetadata;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.epam.ta.reportportal.demodata.service.Constants.*;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class DemoDataTestItemService {

	private final StartTestItemHandler startTestItemHandler;

	private final FinishTestItemHandler finishTestItemHandler;

	@Autowired
	public DemoDataTestItemService(StartTestItemHandler startTestItemHandler, FinishTestItemHandler finishTestItemHandler) {
		this.startTestItemHandler = startTestItemHandler;
		this.finishTestItemHandler = finishTestItemHandler;
	}

	@Transactional
	public String startRootItem(DemoItemMetadata metadata) {

		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(metadata.getName());
		rq.setCodeRef(PACKAGE + metadata.getName());
		rq.setLaunchUuid(metadata.getLaunchId());
		rq.setStartTime(new Date());
		rq.setType(metadata.getType().name());
		if (metadata.getType().sameLevel(SUITE) && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			rq.setAttributes(ContentUtils.getAttributesInRange(ATTRIBUTES_COUNT));
			rq.setDescription(ContentUtils.getSuiteDescription());
		}

		return startTestItemHandler.startRootItem(metadata.getUser(), metadata.getProjectDetails(), rq).getId();
	}

	@Transactional
	public void finishRootItem(String rootItemId, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(new Date());
		finishTestItemHandler.finishTestItem(user, projectDetails, rootItemId, rq);
	}

	@Transactional
	public String startTestItem(DemoItemMetadata metadata) {

		StartTestItemRQ rq = new StartTestItemRQ();
		if (ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			if (hasChildren(metadata.getType())) {
				rq.setAttributes(ContentUtils.getAttributesInRange(ATTRIBUTES_COUNT));
				rq.setDescription(ContentUtils.getTestDescription());
			} else {
				rq.setAttributes(ContentUtils.getAttributesInRange(ATTRIBUTES_COUNT));
				rq.setDescription(ContentUtils.getStepDescription());
			}
		}
		rq.setHasStats(!metadata.isNested());
		rq.setCodeRef(PACKAGE + metadata.getName());
		rq.setRetry(metadata.isRetry());
		rq.setLaunchUuid(metadata.getLaunchId());
		rq.setStartTime(new Date());
		rq.setName(metadata.getName());
		rq.setType(metadata.getType().name());

		return startTestItemHandler.startChildItem(metadata.getUser(), metadata.getProjectDetails(), rq, metadata.getParentId()).getId();
	}

	@Transactional
	public void finishTestItem(String testItemId, StatusEnum status, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(new Date());
		ofNullable(status).ifPresent(it -> {
			rq.setStatus(it.name());
			if (StatusEnum.FAILED.equals(it)) {
				rq.setIssue(issueType());
			}
		});
		finishTestItemHandler.finishTestItem(user, projectDetails, testItemId, rq);
	}

	private boolean hasChildren(TestItemTypeEnum testItemType) {
		return !(testItemType == STEP || testItemType == BEFORE_CLASS || testItemType == BEFORE_METHOD || testItemType == AFTER_CLASS
				|| testItemType == AFTER_METHOD);
	}

	private Issue issueType() {
		int ISSUE_PROBABILITY = 25;
		if (ContentUtils.getWithProbability(ISSUE_PROBABILITY)) { // NOSONAR
			return ContentUtils.getProductBug();
		} else if (ContentUtils.getWithProbability(ISSUE_PROBABILITY)) { // NOSONAR
			return ContentUtils.getAutomationBug();
		} else if (ContentUtils.getWithProbability(ISSUE_PROBABILITY)) { // NOSONAR
			return ContentUtils.getSystemIssue();
		} else {
			return ContentUtils.getInvestigate();
		}
	}
}
