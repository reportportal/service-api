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

import com.epam.ta.reportportal.ws.annotations.In;
import com.epam.ta.reportportal.ws.annotations.NotBlankString;
import com.epam.ta.reportportal.ws.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Edit User request model
 *
 * @author Aliaksandr_Kazantsau
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class EditUserRQ {

	@NotBlankString
	@JsonProperty(value = "email")
	private String email;

	@In(allowedValues = { "user", "administrator" })
	@JsonProperty(value = "role")
	private String role;

	@NotBlankString
	@Size(min = ValidationConstraints.MIN_USER_NAME_LENGTH, max = ValidationConstraints.MAX_USER_NAME_LENGTH)
	@Pattern(regexp = "(\\s*[\\pL0-9-_\\.]+\\s*)+")
	@JsonProperty(value = "fullName")
	private String fullName;

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

	public void setFullName(String value) {
		this.fullName = value;
	}

	public String getFullName() {
		return fullName;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("EditUserRQ{");
		sb.append("email='").append(email).append('\'');
		sb.append(", role='").append(role).append('\'');
		sb.append(", fullName='").append(fullName).append('\'');
		sb.append('}');
		return sb.toString();
	}
}