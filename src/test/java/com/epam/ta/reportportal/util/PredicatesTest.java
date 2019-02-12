package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.junit.Assert;
import org.junit.Test;

import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.util.Predicates.LAUNCH_CAN_BE_INDEXED;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrei Varabyeu
 */
public class PredicatesTest {

	@Test
	public void checkSpecialCharacters() {
		assertTrue("Incorrect predicate behavior: only spec chars", Predicates.SPECIAL_CHARS_ONLY.test("_"));
		Assert.assertFalse("Incorrect predicate behavior: spec chars after ASCII", Predicates.SPECIAL_CHARS_ONLY.test("a_"));
		Assert.assertFalse("Incorrect predicate behavior: spec chars before ASCII", Predicates.SPECIAL_CHARS_ONLY.test("_a"));
	}

	@Test
	public void checkCanBeIndexed() {
		TestItem testItem = new TestItem();
		final TestItemResults itemResults = new TestItemResults();
		final IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIgnoreAnalyzer(false);
		final IssueType issueType = new IssueType();
		issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG));
		issueEntity.setIssueType(issueType);
		itemResults.setIssue(issueEntity);
		testItem.setItemResults(itemResults);
		assertTrue("Item should be available for indexing", ITEM_CAN_BE_INDEXED.test(testItem));
	}

	@Test
	public void checkTIIndexed() {
		TestItem testItem = new TestItem();
		final TestItemResults itemResults = new TestItemResults();
		final IssueEntity issue = new IssueEntity();
		final IssueType issueType = new IssueType();
		issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.TO_INVESTIGATE));
		issueType.setLocator(TestItemIssueGroup.TO_INVESTIGATE.getLocator());
		issue.setIssueType(issueType);
		itemResults.setIssue(issue);
		testItem.setItemResults(itemResults);
		Assert.assertFalse("Item with TI issue shouldn't be available for indexing", ITEM_CAN_BE_INDEXED.test(testItem));
	}

	@Test
	public void checkIgnoreIndexed() {
		TestItem testItem = new TestItem();
		final TestItemResults itemResults = new TestItemResults();
		final IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIgnoreAnalyzer(true);
		final IssueType issueType = new IssueType();
		issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG));
		issueEntity.setIssueType(issueType);
		itemResults.setIssue(issueEntity);
		testItem.setItemResults(itemResults);
		Assert.assertFalse("Item with ignore flag shouldn't be available for indexing", ITEM_CAN_BE_INDEXED.test(testItem));
	}

	@Test
	public void checkLaunchCanBeIndexed() {
		Launch launch = new Launch();
		launch.setMode(LaunchModeEnum.DEFAULT);
		assertTrue("Launch should be available for indexing", LAUNCH_CAN_BE_INDEXED.test(launch));
	}

	@Test
	public void checkDebugLaunchCanBeIndexed() {
		Launch launch = new Launch();
		launch.setMode(LaunchModeEnum.DEFAULT);
		assertTrue("Launch in debug mode should not be available for indexing", LAUNCH_CAN_BE_INDEXED.test(launch));
	}
}