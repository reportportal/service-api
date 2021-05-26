package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class IndexDefectsUpdate {

	@JsonProperty("project")
	private Long projectId;

	@JsonProperty("itemsToUpdate")
	private Map<Long, String> itemsToUpdate;

	public IndexDefectsUpdate(Long projectId, Map<Long, String> itemsToUpdate) {
		this.projectId = projectId;
		this.itemsToUpdate = itemsToUpdate;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Map<Long, String> getItemsToUpdate() {
		return itemsToUpdate;
	}

	public void setItemsToUpdate(Map<Long, String> itemsToUpdate) {
		this.itemsToUpdate = itemsToUpdate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexDefectsUpdate that = (IndexDefectsUpdate) o;
		return Objects.equals(projectId, that.projectId) && Objects.equals(itemsToUpdate, that.itemsToUpdate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, itemsToUpdate);
	}
}
