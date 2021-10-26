package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;

public interface ClusterGenerator {

	void generate(GenerateClustersRq generateClustersRq);

}
