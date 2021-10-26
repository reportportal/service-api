package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.epam.ta.reportportal.ws.converter.converters.ClusterConverter.TO_CLUSTER;
import static java.util.Optional.ofNullable;

@Service
@Transactional
public class CreateClusterHandlerImpl implements CreateClusterHandler {

	private final ClusterRepository clusterRepository;
	private final LogRepository logRepository;

	@Autowired
	public CreateClusterHandlerImpl(ClusterRepository clusterRepository, LogRepository logRepository) {
		this.clusterRepository = clusterRepository;
		this.logRepository = logRepository;
	}

	@Override
	public void create(ClusterData clusterData) {
		ofNullable(clusterData.getClusterInfo()).filter(CollectionUtils::isNotEmpty).ifPresent(clusters -> {
			clusters.stream().filter(c -> Objects.nonNull(c.getClusterId())).forEach(clusterInfoRs -> {
				final Cluster cluster = TO_CLUSTER.apply(clusterInfoRs);
				clusterRepository.save(cluster);
				logRepository.updateClusterIdByIdIn(cluster.getId(), clusterInfoRs.getLogIds());
			});
		});
	}

}
