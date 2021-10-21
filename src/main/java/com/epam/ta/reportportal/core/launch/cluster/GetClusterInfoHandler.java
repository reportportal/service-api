package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.entity.launch.Launch;
import org.springframework.data.domain.Pageable;

public interface GetClusterInfoHandler {

	Iterable<ClusterInfoResource> getResources(Launch launch, Pageable pageable);
}
