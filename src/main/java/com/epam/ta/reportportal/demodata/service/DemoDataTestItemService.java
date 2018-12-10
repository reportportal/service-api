package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.epam.ta.reportportal.demodata.service.Constants.ATTRIBUTES_COUNT;
import static com.epam.ta.reportportal.demodata.service.Constants.CONTENT_PROBABILITY;
import static com.epam.ta.reportportal.entity.enums.TestItemTypeEnum.*;

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
	public Long startRootItem(String rootItemName, Long launchId, TestItemTypeEnum type, ReportPortalUser user,
			ReportPortalUser.ProjectDetails projectDetails) {

		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setName(rootItemName);
		rq.setLaunchId(launchId);
		rq.setStartTime(new Date());
		rq.setType(type.name());
		if (type.sameLevel(SUITE) && ContentUtils.getWithProbability(CONTENT_PROBABILITY)) {
			rq.setAttributes(ContentUtils.getAttributesInRange(ATTRIBUTES_COUNT));
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
				rq.setAttributes(ContentUtils.getAttributesInRange(ATTRIBUTES_COUNT));
				rq.setDescription(ContentUtils.getTestDescription());
			} else {
				rq.setAttributes(ContentUtils.getAttributesInRange(ATTRIBUTES_COUNT));
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
	public void finishTestItem(Long testItemId, StatusEnum status, ReportPortalUser user, ReportPortalUser.ProjectDetails projectDetails) {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(new Date());
		rq.setStatus(status.name());
		if (StatusEnum.FAILED.equals(status)) {
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
