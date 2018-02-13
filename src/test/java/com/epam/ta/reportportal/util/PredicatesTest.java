package com.epam.ta.reportportal.util;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import org.junit.Assert;
import org.junit.Test;

import static com.epam.ta.reportportal.util.Predicates.IS_RETRY;
import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;

/**
 * @author Andrei Varabyeu
 */
public class PredicatesTest {

	@Test
	public void checkSpecialCharacters() {
		Assert.assertTrue("Incorrect predicate behavior: only spec chars", Predicates.SPECIAL_CHARS_ONLY.test("_"));
		Assert.assertFalse("Incorrect predicate behavior: spec chars after ASCII", Predicates.SPECIAL_CHARS_ONLY.test("a_"));
		Assert.assertFalse("Incorrect predicate behavior: spec chars before ASCII", Predicates.SPECIAL_CHARS_ONLY.test("_a"));
	}

	@Test
	public void checkIsRetry() {
		TestItem testItem = new TestItem();
		testItem.setRetryProcessed(false);
		Assert.assertTrue("Item should be a retry", IS_RETRY.test(testItem));
	}

	@Test
	public void negativeCheckIsRetry() {
		TestItem testItem = new TestItem();
		Assert.assertFalse("Item should not be a retry", IS_RETRY.test(testItem));
	}

	@Test
	public void checkCanBeIndexed() {
		TestItem testItem = new TestItem();
		TestItemIssue issue = new TestItemIssue();
		issue.setIgnoreAnalyzer(false);
		issue.setIssueType(TestItemIssueType.PRODUCT_BUG.getLocator());
		testItem.setIssue(issue);
		Assert.assertTrue("Item should be available for indexing", ITEM_CAN_BE_INDEXED.test(testItem));
	}

	@Test
	public void checkTIIndexed() {
		TestItem testItem = new TestItem();
		TestItemIssue issue = new TestItemIssue();
		testItem.setIssue(issue);
		Assert.assertFalse("Item with TI issue shouldn't be available for indexing", ITEM_CAN_BE_INDEXED.test(testItem));
	}
	@Test
	public void checkIgnoreIndexed() {
		TestItem testItem = new TestItem();
		TestItemIssue issue = new TestItemIssue();
		issue.setIssueType(TestItemIssueType.PRODUCT_BUG.getLocator());
		issue.setIgnoreAnalyzer(true);
		testItem.setIssue(issue);
		Assert.assertFalse("Item with ignore flag shouldn't be available for indexing", ITEM_CAN_BE_INDEXED.test(testItem));
	}
}