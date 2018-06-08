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

package com.epam.ta.reportportal.store.database.entity.launch;

import com.epam.ta.reportportal.store.database.entity.enums.TestItemIssueType;

import javax.persistence.Column;

/**
 * @author Pavel Bortnik
 */
public class IssueStatistics {

	@Column(name = "issue_group")
	private TestItemIssueType issueGroup;

	@Column(name = "locator")
	private String locator;

	@Column(name = "total")
	private int total;

	public TestItemIssueType getIssueGroup() {
		return issueGroup;
	}

	public void setIssueGroup(TestItemIssueType issueGroup) {
		this.issueGroup = issueGroup;
	}

	public String getLocator() {
		return locator;
	}

	public void setLocator(String locator) {
		this.locator = locator;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
