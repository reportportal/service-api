package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterInfoRs;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.ws.model.launch.cluster.ClusterInfoResource;

import java.util.function.Function;

public class ClusterConverter {

	private ClusterConverter() {
		//static only
	}

	public static final Function<ClusterInfoRs, Cluster> TO_CLUSTER = rs -> {
		final Cluster cluster = new Cluster();
		cluster.setId(rs.getClusterId());
		cluster.setMessage(rs.getClusterMessage());
		return cluster;
	};

	public static final Function<Cluster, ClusterInfoResource> TO_CLUSTER_INFO = c -> {
		final ClusterInfoResource resource = new ClusterInfoResource();
		resource.setId(c.getId());
		resource.setLaunchId(c.getLaunchId());
		resource.setMessage(c.getMessage());
		return resource;
	};
}
