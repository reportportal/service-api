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

package com.epam.ta.reportportal.model.user;

import com.epam.reportportal.annotations.In;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.NotBlank;

/**
 * Request model for user creation (confirmation will be send on email)
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class CreateUserRQ {

	@NotBlank
	@JsonProperty(value = "email", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String email;

	@NotBlank
	@JsonProperty(value = "role", required = true)
	@In(allowedValues = { "editor", "viewer" })
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String role;

	@NotBlank
	@JsonProperty(value = "defaultProject", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String defaultProject;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDefaultProject() {
		return defaultProject;
	}

	public void setDefaultProject(String value) {
		this.defaultProject = value;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("CreateUserRQ{");
		sb.append("email='").append(email).append('\'');
		sb.append(", role='").append(role).append('\'');
		sb.append(", defaultProject='").append(defaultProject).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
