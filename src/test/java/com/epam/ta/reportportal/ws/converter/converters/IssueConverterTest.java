package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IssueConverterTest {

	@Test
	void toModel() {
		final IssueEntity issueEntity = getIssueEntity();
		final Issue resource = IssueConverter.TO_MODEL.apply(issueEntity);

		assertEquals(resource.getAutoAnalyzed(), issueEntity.getAutoAnalyzed());
		assertEquals(resource.getComment(), issueEntity.getIssueDescription());
		assertEquals(resource.getIgnoreAnalyzer(), issueEntity.getIgnoreAnalyzer());
		assertEquals(resource.getIssueType(), issueEntity.getIssueType().getLocator());
	}

	@Test
	void toResource() {
		final Issue issue = getIssue();
		final IssueEntity issueEntity = IssueConverter.TO_ISSUE.apply(issue);

		assertEquals(issueEntity.getIgnoreAnalyzer(), issue.getIgnoreAnalyzer());
		assertEquals(issueEntity.getAutoAnalyzed(), issue.getAutoAnalyzed());
		assertEquals(issue.getComment(), issue.getComment());
	}

	private static IssueEntity getIssueEntity() {
		final IssueEntity issue = new IssueEntity();
		issue.setIssueType(new IssueType(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG), "locator", "long name", "SNA", "color"));
		issue.setIgnoreAnalyzer(false);
		issue.setAutoAnalyzed(false);
		issue.setIssueDescription("issue description");
		return issue;
	}

	private static Issue getIssue() {
		Issue issue = new Issue();
		issue.setComment("comment");
		issue.setIgnoreAnalyzer(false);
		issue.setAutoAnalyzed(false);
		return issue;
	}
}