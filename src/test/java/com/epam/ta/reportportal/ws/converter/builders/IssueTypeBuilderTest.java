package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class IssueTypeBuilderTest {

	@Test
	public void name() {
		final String color = "color";
		final IssueGroup issueGroup = new IssueGroup(TestItemIssueGroup.PRODUCT_BUG);
		final String locator = "locator";
		final String longName = "longName";
		final String shortName = "shortName";

		final IssueType issueType = new IssueTypeBuilder().addHexColor(color)
				.addIssueGroup(issueGroup)
				.addLocator(locator)
				.addLongName(longName)
				.addShortName(shortName)
				.get();

		assertEquals(color, issueType.getHexColor());
		assertThat(issueType.getIssueGroup()).isEqualToComparingFieldByField(issueGroup);
		assertEquals(locator, issueType.getLocator());
		assertEquals(longName, issueType.getLongName());
		assertEquals(shortName.toUpperCase(), issueType.getShortName());
	}
}