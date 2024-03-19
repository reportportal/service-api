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
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Assign users from project request model
 * 
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class AssignUsersRQ {

	@NotNull
	@JsonProperty(value = "userNames", required = true)
	private Map<String, String> userNames;

	public void setUserNames(Map<String, String> value) {
		this.userNames = value;
	}

	public Map<String, String> getUserNames() {
		return userNames;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("AssignUsersRQ{");
		sb.append("userNames=").append(userNames.keySet());
		sb.append('}');
		return sb.toString();
	}
}