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

package com.epam.ta.reportportal.core.analyzer.auto.impl.preparer;

import com.epam.ta.reportportal.dao.ClusterRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.cluster.Cluster;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.util.Predicates.LAUNCH_CAN_BE_INDEXED;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LaunchPreparerServiceImpl implements LaunchPreparerService {

	private final LaunchRepository launchRepository;
	private final ClusterRepository clusterRepository;

	private final TestItemPreparerService testItemPreparerService;

	@Autowired
	public LaunchPreparerServiceImpl(LaunchRepository launchRepository, ClusterRepository clusterRepository,
			TestItemPreparerService testItemPreparerService) {
		this.launchRepository = launchRepository;
		this.clusterRepository = clusterRepository;
		this.testItemPreparerService = testItemPreparerService;
	}

	@Override
	public Optional<IndexLaunch> prepare(Launch launch, List<TestItem> testItems, AnalyzerConfig analyzerConfig) {
		if (LAUNCH_CAN_BE_INDEXED.test(launch)) {
			final List<IndexTestItem> preparedItems = testItemPreparerService.prepare(launch.getId(), testItems);
			if (CollectionUtils.isNotEmpty(preparedItems)) {
				return Optional.of(createIndexLaunch(launch.getProjectId(),
						launch.getId(),
						launch.getName(),
						analyzerConfig,
						preparedItems
				));
			}
		}
		return Optional.empty();
	}

	private IndexLaunch createIndexLaunch(Long projectId, Long launchId, String name, AnalyzerConfig analyzerConfig,
			List<IndexTestItem> rqTestItems) {
		IndexLaunch rqLaunch = new IndexLaunch();
		rqLaunch.setLaunchId(launchId);
		rqLaunch.setLaunchName(name);
		rqLaunch.setProjectId(projectId);
		rqLaunch.setAnalyzerConfig(analyzerConfig);
		rqLaunch.setTestItems(rqTestItems);
		setClusters(rqLaunch);
		return rqLaunch;
	}

	@Override
	public Optional<IndexLaunch> prepare(Long id, AnalyzerConfig analyzerConfig) {
		return prepare(List.of(id), analyzerConfig).stream().findFirst();
	}

	@Override
	public List<IndexLaunch> prepare(List<Long> ids, AnalyzerConfig analyzerConfig) {
		return launchRepository.findIndexLaunchByIds(ids)
				.stream()
				.peek(this::fill)
				.filter(l -> CollectionUtils.isNotEmpty(l.getTestItems()))
				.peek(l -> l.setAnalyzerConfig(analyzerConfig))
				.collect(Collectors.toList());
	}

	/**
	 * Update prepared launch with items for indexing
	 *
	 * @param indexLaunch - Launch to be updated
	 */
	private void fill(IndexLaunch indexLaunch) {
		final List<IndexTestItem> preparedItems = testItemPreparerService.prepare(indexLaunch.getLaunchId());
		if (!preparedItems.isEmpty()) {
			indexLaunch.setTestItems(preparedItems);
			setClusters(indexLaunch);
		}
	}

	@Override
	public List<IndexLaunch> prepare(AnalyzerConfig analyzerConfig, List<TestItem> testItems) {
		return testItems.stream().collect(Collectors.groupingBy(TestItem::getLaunchId)).entrySet().stream().flatMap(entry -> {
			Launch launch = launchRepository.findById(entry.getKey())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, entry.getKey()));
			return prepare(launch, entry.getValue(), analyzerConfig).stream();
		}).collect(Collectors.toList());
	}

	private void setClusters(IndexLaunch indexLaunch) {
		final Map<Long, String> clusters = clusterRepository.findAllByLaunchId(indexLaunch.getLaunchId())
				.stream()
				.collect(Collectors.toMap(Cluster::getIndexId, Cluster::getMessage));
		if (!clusters.isEmpty()) {
			indexLaunch.setClusters(clusters);
		}
	}

}
