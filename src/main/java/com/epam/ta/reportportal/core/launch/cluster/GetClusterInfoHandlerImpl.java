/*
 * Copyright 2021 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.model.launch.cluster.ClusterInfoResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.ws.converter.converters.ClusterConverter.TO_CLUSTER_INFO;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetClusterInfoHandlerImpl implements GetClusterInfoHandler {

	private final ClusterRepository clusterRepository;

	@Autowired
	public GetClusterInfoHandlerImpl(ClusterRepository clusterRepository) {
		this.clusterRepository = clusterRepository;
	}

	@Override
	public Iterable<ClusterInfoResource> getResources(Launch launch, Pageable pageable) {

		final Pageable pageableWithSort = applySort(pageable);
		final Page<Cluster> clusters = clusterRepository.findAllByLaunchId(launch.getId(), pageableWithSort);

		return PagedResourcesAssembler.pageConverter(TO_CLUSTER_INFO).apply(clusters);
	}

	private Pageable applySort(Pageable pageable) {
		final Sort idSort = Sort.by(Sort.Order.asc(CRITERIA_ID));
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), idSort);
	}

}
