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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;

/**
 * Un-assign users request template
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class UnassignUsersRQ {

	@NotEmpty
	@JsonProperty(value = "userNames", required = true)
	@Schema(requiredMode = RequiredMode.REQUIRED)
	private List<String> usernames;

	public void setUsernames(List<String> value) {
		this.usernames = value;
	}

	public List<String> getUsernames() {
		return usernames;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("UnassignUsersRQ{");
		sb.append("usernames=").append(usernames);
		sb.append('}');
		return sb.toString();
	}
}
