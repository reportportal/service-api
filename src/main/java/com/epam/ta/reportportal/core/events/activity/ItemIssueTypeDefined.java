/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.entity.item.TestItem;
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
