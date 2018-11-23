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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.item.issue.IssueGroup;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class IssueTypeBuilder implements Supplier<IssueType> {

	private IssueType issueType;

	public IssueTypeBuilder() {
		this.issueType = new IssueType();
	}

	public IssueTypeBuilder(IssueType issueType) {
		this.issueType = issueType;
	}

	public IssueTypeBuilder addLocator(String locator) {
		issueType.setLocator(locator);
		return this;
	}

	public IssueTypeBuilder addIssueGroup(IssueGroup issueGroup) {
		issueType.setIssueGroup(issueGroup);
		return this;
	}

	public IssueTypeBuilder addLongName(String longName) {
		issueType.setLongName(longName);
		return this;
	}

	public IssueTypeBuilder addShortName(String shortName) {
		issueType.setShortName(shortName.toUpperCase());
		return this;
	}

	public IssueTypeBuilder addHexColor(String color) {
		issueType.setHexColor(color);
		return this;
	}

	public IssueTypeBuilder addProject(Project project) {
		//		issueType.getProjects().add(project);
		return this;
	}

	//	public IssueTypeBuilder addProjectList(List<Project> projects) {
	//		issueType.getProjects().addAll(projects);
	//		return this;
	//	}

	@Override
	public IssueType get() {
		return issueType;
	}
}
