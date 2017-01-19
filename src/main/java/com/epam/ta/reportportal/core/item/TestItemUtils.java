package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.ws.model.issue.Issue;

import java.util.Date;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * DB<->DTO conversion utils for TestItem
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
class TestItemUtils {

	/**
	 * Converts DB model to DTO
	 */
	static Function<TestItemIssue, Issue> ISSUE_CONVERTER = new Function<TestItemIssue, Issue>() {
		@Override
		public Issue apply(TestItemIssue itemIssue) {
			Issue issue = new Issue();
			issue.setComment(itemIssue.getIssueDescription());
			issue.setIssueType(itemIssue.getIssueType());
			if (null != itemIssue.getExternalSystemIssues()) {
				issue.setExternalSystemIssues(itemIssue.getExternalSystemIssues().stream().map(EXTERNAL_ISSUE_CONVERTER).collect(toSet()));
			}
			return issue;
		}
	};

	/**
	 * Converts DB model to DTO
	 */
	private static Function<TestItemIssue.ExternalSystemIssue, Issue.ExternalSystemIssue> EXTERNAL_ISSUE_CONVERTER = externalSystemIssue -> {
		Issue.ExternalSystemIssue dbExternalSystemIssue = new Issue.ExternalSystemIssue();
		dbExternalSystemIssue.setSubmitDate(externalSystemIssue.getSubmitDate());
		dbExternalSystemIssue.setSubmitter(externalSystemIssue.getSubmitter());
		dbExternalSystemIssue.setTicketId(externalSystemIssue.getTicketId());
		dbExternalSystemIssue.setExternalSystemId(externalSystemIssue.getExternalSystemId());
		dbExternalSystemIssue.setUrl(externalSystemIssue.getUrl());
		return dbExternalSystemIssue;
	};

	/**
	 * Converts DTO to DB model
	 *
	 * @param userName Name of user
	 * @return Conversion function
	 */
	static Function<Issue.ExternalSystemIssue, TestItemIssue.ExternalSystemIssue> externalIssueDtoConverter(String userName) {
		return issue -> {
			TestItemIssue.ExternalSystemIssue externalSystemIssue = new TestItemIssue.ExternalSystemIssue();
			externalSystemIssue.setTicketId(issue.getTicketId().trim());
			externalSystemIssue.setSubmitDate(new Date().getTime());
			externalSystemIssue.setSubmitter(userName);
			externalSystemIssue.setExternalSystemId(externalSystemIssue.getExternalSystemId());
			externalSystemIssue.setUrl(issue.getUrl());
			return externalSystemIssue;
		};
	}

	/**
	 * Converts DTO model to DB keeping specified external system ID
	 *
	 * @param externalSystemId External system ID to use
	 * @param userName         Name of user
	 * @return Conversion function
	 */
	static Function<Issue.ExternalSystemIssue, TestItemIssue.ExternalSystemIssue> externalIssueDtoConverter(String externalSystemId,
			String userName) {
		return externalIssueDtoConverter(userName).andThen(externalSystemIssue -> {
			externalSystemIssue.setExternalSystemId(externalSystemId);
			return externalSystemIssue;
		});
	}

}
