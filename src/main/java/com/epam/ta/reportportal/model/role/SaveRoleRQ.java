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
 
package com.epam.ta.reportportal.model.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

/**
 * 
 * @author Siarhei_Kharlanau
 * 
 */
@JsonInclude(Include.NON_NULL)
public class SaveRoleRQ {

	@NotNull
	@JsonProperty(value = "roleName", required = true)
	private String roleName;

	@NotNull
	@JsonProperty(value = "permissions", required = true)
	private String permissions;

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("SaveRoleRQ{");
		sb.append("roleName='").append(roleName).append('\'');
		sb.append(", permissions='").append(permissions).append('\'');
		sb.append('}');
		return sb.toString();
	}

}