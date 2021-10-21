package com.epam.ta.reportportal.core.launch.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClusterInfoResource {

	@JsonProperty(value = "id")
	private Long id;

	@JsonProperty(value = "launchId")
	private Long launchId;

	@JsonProperty(value = "message")
	private String message;

	public ClusterInfoResource() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
