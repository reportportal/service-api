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
import com.epam.ta.reportportal.ws.model.TestItemResource;

import java.util.List;
import java.util.Map;

/**
 * @author Andrei Varabyeu
 */
public class TicketAttachedEvent extends AroundEvent<List<TestItem>> {

	private final String postedBy;
	private final String project;
	private final Map<String, TestItemResource> relevantItemMap;

	public TicketAttachedEvent(List<TestItem> before, List<TestItem> after, String postedBy, String project,
			Map<String, TestItemResource> relevantItemMap) {
		super(before, after);
		this.postedBy = postedBy;
		this.project = project;
		this.relevantItemMap = relevantItemMap;
	}

	public Map<String, TestItemResource> getRelevantItemMap() {
		return relevantItemMap;
	}

	public String getPostedBy() {
		return postedBy;
	}

	public String getProject() {
		return project;
	}
}
