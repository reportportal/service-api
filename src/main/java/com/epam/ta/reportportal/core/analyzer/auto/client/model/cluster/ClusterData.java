package com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster;

import java.util.List;

public class ClusterData {

	private Long project;
	private Long launchId;
	private List<ClusterInfoRs> clusterInfo;

	public ClusterData() {
	}

	public Long getProject() {
		return project;
	}

	public void setProject(Long project) {
		this.project = project;
	}

	public Long getLaunchId() {
		return launchId;
	}

	public void setLaunchId(Long launchId) {
		this.launchId = launchId;
	}

	public List<ClusterInfoRs> getClusterInfo() {
		return clusterInfo;
	}

	public void setClusterInfo(List<ClusterInfoRs> clusterInfo) {
		this.clusterInfo = clusterInfo;
	}
}
