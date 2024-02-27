/*
 * Copyright 2019 EPAM Systems
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
 
package com.epam.ta.reportportal.model.issue;

import com.epam.ta.reportportal.ws.reporting.Issue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Test item issue with provided it
 * 
 * @author Dzianis Shlychkou
 * 
 */
@JsonInclude(Include.NON_NULL)
public class IssueDefinition {

	@NotNull
	@JsonProperty(value = "testItemId", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private Long id;

	@NotNull
	@Valid
	@JsonProperty(value = "issue", required = true)
	private Issue issue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	@Override
	public String toString() {
		return "IssueDefinition{" + "id=" + id + ", issue=" + issue + '}';
	}
}