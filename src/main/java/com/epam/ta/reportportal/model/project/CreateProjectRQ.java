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

package com.epam.ta.reportportal.model.project;

import static com.epam.ta.reportportal.ws.model.ValidationConstraints.PROJECT_NAME_REGEXP;

import com.epam.ta.reportportal.ws.annotations.In;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Create project request initial model
 *
 * @author Hanna_Sukhadolava
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class CreateProjectRQ {

	@NotBlank
	@Pattern(regexp = PROJECT_NAME_REGEXP)
	@Size(min = ValidationConstraints.MIN_NAME_LENGTH, max = ValidationConstraints.MAX_NAME_LENGTH)
	@JsonProperty(value = "projectName", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String projectName;

	@NotBlank
	@JsonProperty(value = "entryType", required = true)
	@In(allowedValues = "internal")
	@Schema(required = true, allowableValues = "INTERNAL")
	private String entryType;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getEntryType() {
		return entryType;
	}

	public void setEntryType(String value) {
		this.entryType = value;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CreateProjectRQ{");
		sb.append("projectName='").append(projectName).append('\'');
		sb.append(", entryType='").append(entryType).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
