/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;

import java.util.Map;

/**
 * @author Andrei Varabyeu
 */
public class ItemIssueTypeDefined {

	private final String postedBy;
	private final Map<IssueDefinition, TestItem> before;
	private final String project;

	public ItemIssueTypeDefined(Map<IssueDefinition, TestItem> before, String postedBy, String project) {
		this.postedBy = postedBy;
		this.before = before;
		this.project = project;
	}

	public String getPostedBy() {
		return postedBy;
	}

	public Map<IssueDefinition, TestItem> getBefore() {
		return before;
	}

	public String getProject() {
		return project;
	}

}
