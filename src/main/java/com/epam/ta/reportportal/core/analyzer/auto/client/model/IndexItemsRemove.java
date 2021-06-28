/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class IndexItemsRemove {

	@JsonProperty("project")
	private Long projectId;

	@JsonProperty("itemsToDelete")
	private List<Long> itemsToDelete;

	public IndexItemsRemove(Long projectId, List<Long> itemsToDelete) {
		this.projectId = projectId;
		this.itemsToDelete = itemsToDelete;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public List<Long> getItemsToDelete() {
		return itemsToDelete;
	}

	public void setItemsToDelete(List<Long> itemsToDelete) {
		this.itemsToDelete = itemsToDelete;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexItemsRemove that = (IndexItemsRemove) o;
		return Objects.equals(projectId, that.projectId) && Objects.equals(itemsToDelete, that.itemsToDelete);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, itemsToDelete);
	}
}
