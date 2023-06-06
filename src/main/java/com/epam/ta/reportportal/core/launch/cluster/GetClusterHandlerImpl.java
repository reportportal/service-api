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

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.ws.converter.converters.ClusterConverter.TO_CLUSTER_INFO;

import com.epam.reportportal.extension.event.GetClusterResourcesEvent;
import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.cluster.ClusterInfoResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class GetClusterHandlerImpl implements GetClusterHandler {

  private final ClusterRepository clusterRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public GetClusterHandlerImpl(ClusterRepository clusterRepository,
      ApplicationEventPublisher eventPublisher) {
    this.clusterRepository = clusterRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public Cluster getById(Long id) {
    return clusterRepository.findById(id)
        .orElseThrow(() -> new ReportPortalException(ErrorType.CLUSTER_NOT_FOUND, id));
  }

  @Override
  public Iterable<ClusterInfoResource> getResources(Launch launch, Pageable pageable) {

    final Pageable pageableWithSort = applySort(pageable);
    final Page<Cluster> clusters = clusterRepository.findAllByLaunchId(launch.getId(),
        pageableWithSort);

    return getClusterResources(clusters, launch.getId());
  }

  private Pageable applySort(Pageable pageable) {
    final Sort idSort = Sort.by(Sort.Order.asc(CRITERIA_ID));
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), idSort);
  }

  private Iterable<ClusterInfoResource> getClusterResources(Page<Cluster> clusters, Long launchId) {
    final com.epam.ta.reportportal.ws.model.Page<ClusterInfoResource> clustersPage = PagedResourcesAssembler.pageConverter(
        TO_CLUSTER_INFO).apply(clusters);
    eventPublisher.publishEvent(new GetClusterResourcesEvent(clustersPage.getContent(), launchId));
    return clustersPage;
  }

}
