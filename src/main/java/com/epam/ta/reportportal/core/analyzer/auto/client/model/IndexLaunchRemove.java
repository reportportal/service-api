package com.epam.ta.reportportal.core.analyzer.auto.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class IndexLaunchRemove {

	@JsonProperty("project")
	private Long projectId;

	@JsonProperty("launch_ids")
	private List<Long> launchIds;

	public IndexLaunchRemove(Long projectId, List<Long> launchIds) {
		this.projectId = projectId;
		this.launchIds = launchIds;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public List<Long> getLaunchIds() {
		return launchIds;
	}

	public void setLaunchIds(List<Long> launchIds) {
		this.launchIds = launchIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		IndexLaunchRemove that = (IndexLaunchRemove) o;
		return Objects.equals(projectId, that.projectId) && Objects.equals(launchIds, that.launchIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, launchIds);
	}
}
