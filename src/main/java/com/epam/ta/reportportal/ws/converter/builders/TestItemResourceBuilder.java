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

package com.epam.ta.reportportal.ws.converter.builders;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.issue.Issue;

/**
 * Builder for {@link TestItemResource} domain object.
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
@Scope("prototype")
public class TestItemResourceBuilder extends Builder<TestItemResource> {

	@Override
	protected TestItemResource initObject() {
		return new TestItemResource();
	}

	public TestItemResourceBuilder addTestItem(TestItem item, String launchStatus) {
		if (item == null) {
			return this;
		}
		TestItemResource resource = getObject();
		resource.setDescription(item.getItemDescription());
		resource.setTags(item.getTags());
		resource.setEndTime(item.getEndTime());
		resource.setItemId(item.getId());

		TestItemIssue testItemIssue = item.getIssue();
		if (null != testItemIssue) {
			Issue issue = new Issue();
			issue.setIssueType(testItemIssue.getIssueType());
			issue.setComment(testItemIssue.getIssueDescription());
			Set<TestItemIssue.ExternalSystemIssue> externalSystemIssues = testItemIssue.getExternalSystemIssues();
			if (null != externalSystemIssues) {
				Set<Issue.ExternalSystemIssue> issuesResource = new HashSet<>();
				for (TestItemIssue.ExternalSystemIssue externalSystemIssue : externalSystemIssues) {
					Issue.ExternalSystemIssue issueResource = new Issue.ExternalSystemIssue();
					issueResource.setSubmitDate(externalSystemIssue.getSubmitDate());
					issueResource.setTicketId(externalSystemIssue.getTicketId());
					issueResource.setSubmitter(externalSystemIssue.getSubmitter());
					issueResource.setExternalSystemId(externalSystemIssue.getExternalSystemId());
					issueResource.setUrl(externalSystemIssue.getUrl());
					issuesResource.add(issueResource);
				}
				issue.setExternalSystemIssues(issuesResource);
			}
			resource.setIssue(issue);
		}

		resource.setName(item.getName());
		resource.setStartTime(item.getStartTime());
		resource.setStatus(item.getStatus() != null ? item.getStatus().toString() : null);
		resource.setType(item.getType() != null ? item.getType().name() : null);
		resource.setParent(item.getParent() != null ? item.getParent() : null);
		resource.setHasChilds(item.hasChilds());
		resource.setLaunchStatus(launchStatus);
		resource.setLaunchId(item.getLaunchRef());

		Statistics statistics = item.getStatistics();
		if (statistics != null) {
			com.epam.ta.reportportal.ws.model.statistics.Statistics statisticsCounters = new com.epam.ta.reportportal.ws.model.statistics.Statistics();
			ExecutionCounter executionCounter = statistics.getExecutionCounter();
			if (executionCounter != null) {
				com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter execution = new com.epam.ta.reportportal.ws.model.statistics.ExecutionCounter();
				execution.setTotal(executionCounter.getTotal().toString());
				execution.setPassed(executionCounter.getPassed().toString());
				execution.setFailed(executionCounter.getFailed().toString());
				execution.setSkipped(executionCounter.getSkipped().toString());
				statisticsCounters.setExecutions(execution);
			}
			IssueCounter issueCounter = statistics.getIssueCounter();
			if (issueCounter != null) {
				com.epam.ta.reportportal.ws.model.statistics.IssueCounter issues = new com.epam.ta.reportportal.ws.model.statistics.IssueCounter();
				issues.setProductBug(issueCounter.getProductBug());
				issues.setSystemIssue(issueCounter.getSystemIssue());
				issues.setAutomationBug(issueCounter.getAutomationBug());
				issues.setToInvestigate(issueCounter.getToInvestigate());
				issues.setNoDefect(issueCounter.getNoDefect());
				statisticsCounters.setDefects(issues);
			}
			resource.setStatistics(statisticsCounters);
		}
		return this;
	}

	public TestItemResourceBuilder addPathNames(Map<String, String> pathNames) {
		getObject().setPathNames(pathNames);
		return this;
	}
}
