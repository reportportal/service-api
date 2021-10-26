package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;

public interface CreateClusterHandler {

	void create(ClusterData clusterData);
}
