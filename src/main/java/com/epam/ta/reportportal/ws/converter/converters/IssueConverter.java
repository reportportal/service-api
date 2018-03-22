/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.store.database.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.ws.model.issue.Issue;

import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class IssueConverter {

	private IssueConverter() {
		//static only
	}

	public static final Function<Issue, IssueEntity> TO_ISSUE = from -> {
		IssueEntity issue = new IssueEntity();
		issue.setAutoAnalyzed(from.getAutoAnalyzed());
		issue.setIgnoreAnalyzer(from.getIgnoreAnalyzer());
		issue.setIssueDescription(from.getComment());
		return issue;
	};

	//	/**
	//	 * Converts issue from db to model
	//	 */
	//	public static final Function<TestItemIssue, Issue> TO_MODEL = testItemIssue -> {
	//		Issue issue = null;
	//		if (null != testItemIssue) {
	//			issue = new Issue();
	//			issue.setIssueType(testItemIssue.getIssueType());
	//			issue.setAutoAnalyzed(testItemIssue.isAutoAnalyzed());
	//			issue.setIgnoreAnalyzer(testItemIssue.isIgnoreAnalyzer());
	//			issue.setComment(testItemIssue.getIssueDescription());
	//			Set<TestItemIssue.ExternalSystemIssue> externalSystemIssues = testItemIssue.getExternalSystemIssues();
	//			if (null != externalSystemIssues) {
	//				issue.setExternalSystemIssues(
	//						externalSystemIssues.stream().map(IssueConverter.TO_MODEL_EXTERNAL).collect(Collectors.toSet()));
	//			}
	//		} return issue;
	//	};
	//
	//	/**
	//	 * Converts external system from db to model
	//	 */
	//	public static final Function<TestItemIssue.ExternalSystemIssue, Issue.ExternalSystemIssue> TO_MODEL_EXTERNAL = externalSystemIssue -> {
	//		Issue.ExternalSystemIssue issueResource = new Issue.ExternalSystemIssue();
	//		issueResource.setSubmitDate(externalSystemIssue.getSubmitDate());
	//		issueResource.setTicketId(externalSystemIssue.getTicketId());
	//		issueResource.setSubmitter(externalSystemIssue.getSubmitter());
	//		issueResource.setExternalSystemId(externalSystemIssue.getExternalSystemId());
	//		issueResource.setUrl(externalSystemIssue.getUrl());
	//		return issueResource;
	//	};
}
