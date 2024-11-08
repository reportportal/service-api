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

import com.epam.reportportal.model.ValidationConstraints;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author Dzmitry_Kavalets
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResetPasswordRQ {

	@NotBlank
	@Size(min = ValidationConstraints.MIN_PASSWORD_LENGTH, max = ValidationConstraints.MAX_PASSWORD_LENGTH)
	@JsonProperty(value = "password")
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String password;

	@NotBlank
	@JsonProperty(value = "uuid")
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String uuid;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ResetPasswordRQ that = (ResetPasswordRQ) o;

		if (password != null ? !password.equals(that.password) : that.password != null) {
			return false;
		}
		return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);

	}

	@Override
	public int hashCode() {
		int result = password != null ? password.hashCode() : 0;
		result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RestorePasswordRQ{");
		sb.append("password='").append(password).append('\''); //NOSONAR
		sb.append(", uuid='").append(uuid).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
