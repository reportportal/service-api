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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Dzmitry_Kavalets
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestorePasswordRQ {

	@NotBlank
	@JsonProperty(value = "email")
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private String email;

  @Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		RestorePasswordRQ that = (RestorePasswordRQ) o;

		return !(email != null ? !email.equals(that.email) : that.email != null);

	}

	@Override
	public int hashCode() {
		return email != null ? email.hashCode() : 0;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ResetPasswordRQ{");
		sb.append("email='").append(email).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
