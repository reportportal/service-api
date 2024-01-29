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
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * User resource representation for responses
 *
 * @author Andrei_Ramanchuk
 */
@JsonInclude(Include.NON_NULL)
public class UserResource {

	@NotNull
	@JsonProperty(value = "id", required = true)
	private Long id;

	@NotNull
	@JsonProperty(value = "userId", required = true)
	private String userId;

	@JsonProperty(value = "email", required = true)
	private String email;

	@JsonProperty(value = "photoId")
	private String photoId;

	@JsonProperty(value = "fullName")
	private String fullName;

	@JsonProperty(value = "accountType")
	private String accountType;

	@JsonProperty(value = "userRole")
	private String userRole;

	@JsonProperty(value = "photoLoaded")
	private boolean isLoaded;

	@JsonProperty(value = "metadata")
	private Object metadata;

	@JsonProperty(value = "assignedProjects")
	private Map<String, AssignedProject> assignedProjects;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUserId(String value) {
		this.userId = value;
	}

	public String getUserId() {
		return userId;
	}

	public void setEmail(String value) {
		this.email = value;
	}

	public String getEmail() {
		return email;
	}

	public void setPhotoId(String value) {
		this.photoId = value;
	}

	public String getPhotoId() {
		return photoId;
	}

	public void setFullName(String value) {
		this.fullName = value;
	}

	public String getFullName() {
		return fullName;
	}

	public void setAccountType(String value) {
		this.accountType = value;
	}

	public String getAccountType() {
		return accountType;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String value) {
		this.userRole = value;
	}

	public Object getMetadata() {
		return metadata;
	}

	public void setMetadata(Object metadata) {
		this.metadata = metadata;
	}

	public void setIsLoaded(boolean value) {
		this.isLoaded = value;
	}

	public boolean getIsLoaded() {
		return isLoaded;
	}

	public Map<String, AssignedProject> getAssignedProjects() {
		return assignedProjects;
	}

	public void setAssignedProjects(Map<String, AssignedProject> assignedProjects) {
		this.assignedProjects = assignedProjects;
	}

	public static class AssignedProject {

		private String projectRole;
		private String entryType;

		public String getEntryType() {
			return entryType;
		}

		public void setEntryType(String entryType) {
			this.entryType = entryType;
		}

		public void setProjectRole(String projectRole) {
			this.projectRole = projectRole;
		}

		public String getProjectRole() {
			return projectRole;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder("AssignedProject{");
			sb.append("projectRole='").append(projectRole).append('\'');
			sb.append('}');
			return sb.toString();
		}
	}

	@Override
	public String toString() {
		return "UserResource{" + "id=" + id + ", userId='" + userId + '\'' + ", email='" + email + '\'' + ", photoId='" + photoId + '\''
				+ ", fullName='" + fullName + '\'' + ", accountType='" + accountType + '\'' + ", userRole='" + userRole + '\''
				+ ", isLoaded=" + isLoaded + ", metadata=" + metadata + ", assignedProjects=" + assignedProjects + '}';
	}
}
