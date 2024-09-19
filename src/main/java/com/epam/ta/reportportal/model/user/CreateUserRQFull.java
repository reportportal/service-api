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
import com.epam.reportportal.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Create User request for admin user creation functionality
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class CreateUserRQFull {

	@NotBlank
	@Pattern(regexp = "[a-zA-Z0-9-_.]+")
	@Size(min = ValidationConstraints.MIN_LOGIN_LENGTH, max = ValidationConstraints.MAX_LOGIN_LENGTH)
	@JsonProperty(value = "login", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
	private String login;

	@NotBlank
	@Size(min = ValidationConstraints.MIN_PASSWORD_LENGTH, max = ValidationConstraints.MAX_PASSWORD_LENGTH)
	@JsonProperty(value = "password", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String password;

	@NotBlank
	@Pattern(regexp = "[\\pL0-9-_ \\.]+")
	@Size(min = ValidationConstraints.MIN_USER_NAME_LENGTH, max = ValidationConstraints.MAX_USER_NAME_LENGTH)
	@JsonProperty(value = "fullName", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "string")
	private String fullName;

	@NotBlank
	@JsonProperty(value = "email", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String email;

	@NotNull
	@JsonProperty(value = "accountRole", required = true)
	@In(allowedValues = { "user", "administrator" })
	@Schema(required = true, allowableValues = "USER, ADMINISTRATOR")
	private String accountRole;

	@NotNull
	@JsonProperty(value = "projectRole", required = true)
	@In(allowedValues = { "operator", "customer", "member", "project_manager" })
	@Schema(required = true, allowableValues = "CUSTOMER, MEMBER, PROJECT_MANAGER")
	private String projectRole;

	@NotBlank
	@JsonProperty(value = "defaultProject", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String defaultProject;

	public void setLogin(String value) {
		this.login = value;
	}

	public String getLogin() {
		return login;
	}

	public void setPassword(String value) {
		this.password = value;
	}

	public String getPassword() {
		return password;
	}

	public void setFullName(String value) {
		this.fullName = value;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAccountRole() {
		return accountRole;
	}

	public void setAccountRole(String role) {
		this.accountRole = role;
	}

	public String getProjectRole() {
		return projectRole;
	}

	public void setProjectRole(String role) {
		this.projectRole = role;
	}

	public String getDefaultProject() {
		return defaultProject;
	}

	public void setDefaultProject(String value) {
		this.defaultProject = value;
	}

	@Override
	public String toString() {
		return "CreateUserRQFull [login=" + login + ", password=" + password + ", fullName=" + fullName + ", email=" + email
				+ ", projectRole=" + projectRole + ", defaultProject=" + defaultProject + "]";
	}
}