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

package com.epam.ta.reportportal.core.launch.cluster.pipeline.data;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class AnalyzerItemClusterDataProvider extends AnalyzerClusterDataProvider {

	private final GetLaunchHandler getLaunchHandler;
	private final TestItemRepository testItemRepository;
	private final LaunchPreparerService launchPreparerService;

	public AnalyzerItemClusterDataProvider(AnalyzerServiceClient analyzerServiceClient, GetLaunchHandler getLaunchHandler,
			TestItemRepository testItemRepository, LaunchPreparerService launchPreparerService) {
		super(analyzerServiceClient);
		this.getLaunchHandler = getLaunchHandler;
		this.testItemRepository = testItemRepository;
		this.launchPreparerService = launchPreparerService;
	}

	@Override
	protected Optional<IndexLaunch> prepareIndexLaunch(GenerateClustersConfig config) {
		final ClusterEntityContext entityContext = config.getEntityContext();
		if (CollectionUtils.isEmpty(entityContext.getItemIds())) {
			return Optional.empty();
		}
		final Launch launch = getLaunchHandler.get(entityContext.getLaunchId());
		final List<TestItem> testItems = testItemRepository.findAllById(entityContext.getItemIds());
		return launchPreparerService.prepare(launch, testItems, config.getAnalyzerConfig());
	}
}
