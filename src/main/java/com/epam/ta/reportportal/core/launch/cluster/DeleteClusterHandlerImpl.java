package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.dao.ClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteClusterHandlerImpl implements DeleteClusterHandler {

	private final ClusterRepository clusterRepository;

	@Autowired
	public DeleteClusterHandlerImpl(ClusterRepository clusterRepository) {
		this.clusterRepository = clusterRepository;
	}

	@Override
	public void deleteLaunchClusters(Long launchId) {
		clusterRepository.deleteAllByLaunchId(launchId);
	}
}
