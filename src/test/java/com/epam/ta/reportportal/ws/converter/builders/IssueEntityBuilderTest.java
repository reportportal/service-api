package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IssueEntityBuilderTest {

	@Test
	void issueEntityBuilder() {
		final boolean autoAnalyzed = false;
		final boolean ignoreAnalyzer = true;
		final IssueType issueType = new IssueType(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG),
				"locator",
				"longName",
				"shortName",
				"color"
		);
		final String description = "description";

		final IssueEntity issueEntity = new IssueEntityBuilder().addAutoAnalyzedFlag(autoAnalyzed)
				.addIgnoreFlag(ignoreAnalyzer)
				.addIssueType(issueType)
				.addDescription(description)
				.get();

		assertEquals(autoAnalyzed, issueEntity.getAutoAnalyzed());
		assertEquals(ignoreAnalyzer, issueEntity.getIgnoreAnalyzer());
		assertThat(issueEntity.getIssueType()).isEqualToComparingFieldByField(issueType);
		assertEquals(description, issueEntity.getIssueDescription());
	}
}