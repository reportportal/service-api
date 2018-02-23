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

package com.epam.ta.reportportal.store.database.entity.item;

import com.epam.ta.reportportal.store.database.entity.item.issue.Issue;
import com.epam.ta.reportportal.store.database.entity.item.issue.IssueType;

import java.util.Objects;

/**
 * @author Pavel Bortnik
 */
public class TestItemCommon {

	private TestItem testItem;

	private TestItemResults testItemResults;

	private TestItemStructure testItemStructure;

	private Issue issue;

	private IssueType issueType;

	public TestItemCommon() {
	}

	public TestItemCommon(TestItem testItem, TestItemResults testItemResults, TestItemStructure testItemStructure, Issue issue,
			IssueType issueType) {
		this.testItem = testItem;
		this.testItemResults = testItemResults;
		this.testItemStructure = testItemStructure;
		this.issue = issue;
		this.issueType = issueType;
	}

	public TestItem getTestItem() {
		return testItem;
	}

	public void setTestItem(TestItem testItem) {
		this.testItem = testItem;
	}

	public TestItemResults getTestItemResults() {
		return testItemResults;
	}

	public void setTestItemResults(TestItemResults testItemResults) {
		this.testItemResults = testItemResults;
	}

	public TestItemStructure getTestItemStructure() {
		return testItemStructure;
	}

	public void setTestItemStructure(TestItemStructure testItemStructure) {
		this.testItemStructure = testItemStructure;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public IssueType getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueType issueType) {
		this.issueType = issueType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TestItemCommon that = (TestItemCommon) o;
		return Objects.equals(testItem, that.testItem) && Objects.equals(testItemResults, that.testItemResults) && Objects.equals(
				testItemStructure, that.testItemStructure) && Objects.equals(issue, that.issue) && Objects.equals(
				issueType, that.issueType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(testItem, testItemResults, testItemStructure, issue, issueType);
	}

	@Override
	public String toString() {
		return "TestItemCommon{" + "testItem=" + testItem + ", testItemResults=" + testItemResults + ", testItemStructure="
				+ testItemStructure + ", issue=" + issue + ", issueType=" + issueType + '}';
	}

}
