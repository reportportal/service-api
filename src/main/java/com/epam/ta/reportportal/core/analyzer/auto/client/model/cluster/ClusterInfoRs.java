package com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster;

import java.util.List;

public class ClusterInfoRs {

	private Long clusterId;
	private String clusterMessage;
	private List<Long> logIds;

	public ClusterInfoRs() {
	}

	public Long getClusterId() {
		return clusterId;
	}

	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterMessage() {
		return clusterMessage;
	}

	public void setClusterMessage(String clusterMessage) {
		this.clusterMessage = clusterMessage;
	}

	public List<Long> getLogIds() {
		return logIds;
	}

	public void setLogIds(List<Long> logIds) {
		this.logIds = logIds;
	}
}
