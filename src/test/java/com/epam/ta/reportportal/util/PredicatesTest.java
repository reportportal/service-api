package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import org.junit.jupiter.api.Test;

import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.util.Predicates.LAUNCH_CAN_BE_INDEXED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Andrei Varabyeu
 */
class PredicatesTest {

	@Test
	void checkSpecialCharacters() {
		assertTrue(Predicates.SPECIAL_CHARS_ONLY.test("_"), "Incorrect predicate behavior: only spec chars");
		assertFalse(Predicates.SPECIAL_CHARS_ONLY.test("a_"), "Incorrect predicate behavior: spec chars after ASCII");
		assertFalse(Predicates.SPECIAL_CHARS_ONLY.test("_a"), "Incorrect predicate behavior: spec chars before ASCII");
	}

	@Test
	void checkCanBeIndexed() {
		TestItem testItem = new TestItem();
		final TestItemResults itemResults = new TestItemResults();
		final IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIgnoreAnalyzer(false);
		final IssueType issueType = new IssueType();
		issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG));
		issueEntity.setIssueType(issueType);
		itemResults.setIssue(issueEntity);
		testItem.setItemResults(itemResults);
		assertTrue(ITEM_CAN_BE_INDEXED.test(testItem), "Item should be available for indexing");
	}

	@Test
	void checkTIIndexed() {
		TestItem testItem = new TestItem();
		final TestItemResults itemResults = new TestItemResults();
		final IssueEntity issue = new IssueEntity();
		final IssueType issueType = new IssueType();
		issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.TO_INVESTIGATE));
		issueType.setLocator(TestItemIssueGroup.TO_INVESTIGATE.getLocator());
		issue.setIssueType(issueType);
		itemResults.setIssue(issue);
		testItem.setItemResults(itemResults);
		assertFalse(ITEM_CAN_BE_INDEXED.test(testItem), "Item with TI issue shouldn't be available for indexing");
	}

	@Test
	void checkIgnoreIndexed() {
		TestItem testItem = new TestItem();
		final TestItemResults itemResults = new TestItemResults();
		final IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIgnoreAnalyzer(true);
		final IssueType issueType = new IssueType();
		issueType.setIssueGroup(new IssueGroup(TestItemIssueGroup.PRODUCT_BUG));
		issueEntity.setIssueType(issueType);
		itemResults.setIssue(issueEntity);
		testItem.setItemResults(itemResults);
		assertFalse(ITEM_CAN_BE_INDEXED.test(testItem), "Item with ignore flag shouldn't be available for indexing");
	}

	@Test
	void checkLaunchCanBeIndexed() {
		Launch launch = new Launch();
		launch.setMode(LaunchModeEnum.DEFAULT);
		assertTrue(LAUNCH_CAN_BE_INDEXED.test(launch), "Launch should be available for indexing");
	}

	@Test
	void checkDebugLaunchCanBeIndexed() {
		Launch launch = new Launch();
		launch.setMode(LaunchModeEnum.DEFAULT);
		assertTrue(LAUNCH_CAN_BE_INDEXED.test(launch), "Launch in debug mode should not be available for indexing");
	}
}