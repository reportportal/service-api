package com.epam.ta.reportportal.core.analyzer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class CleanIndexRq {

	@JsonProperty("item_id")
	private Long projectId;

	@JsonProperty("item_ids")
	private List<Long> itemIds;

	public CleanIndexRq() {
	}

	public CleanIndexRq(Long projectId, List<Long> itemIds) {
		this.projectId = projectId;
		this.itemIds = itemIds;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public List<Long> getItemIds() {
		return itemIds;
	}

	public void setItemIds(List<Long> itemIds) {
		this.itemIds = itemIds;
	}
}
