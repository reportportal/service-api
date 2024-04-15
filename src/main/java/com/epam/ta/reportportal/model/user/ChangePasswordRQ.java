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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Dzmitry_Kavalets
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangePasswordRQ {

	@NotBlank
	@Size(min = ValidationConstraints.MIN_PASSWORD_LENGTH, max = ValidationConstraints.MAX_PASSWORD_LENGTH)
	private String newPassword;

	@NotBlank
	@Size(min = ValidationConstraints.MIN_PASSWORD_LENGTH, max = ValidationConstraints.MAX_PASSWORD_LENGTH)
	private String oldPassword;

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ChangePasswordRQ that = (ChangePasswordRQ) o;

		if (newPassword != null ? !newPassword.equals(that.newPassword) : that.newPassword != null) {
			return false;
		}
		return !(oldPassword != null ? !oldPassword.equals(that.oldPassword) : that.oldPassword != null);

	}

	@Override
	public int hashCode() {
		int result = newPassword != null ? newPassword.hashCode() : 0;
		result = 31 * result + (oldPassword != null ? oldPassword.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ChangePasswordRQ{");
		sb.append("newPassword='").append(newPassword).append('\''); //NOSONAR
		sb.append(", oldPassword='").append(oldPassword).append('\''); //NOSONAR
		sb.append('}');
		return sb.toString();
	}
}