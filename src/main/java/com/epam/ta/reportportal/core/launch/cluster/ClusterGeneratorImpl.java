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

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersConfig;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ClusterGeneratorImpl implements ClusterGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterGeneratorImpl.class);

	public static final String RP_CLUSTER_LAST_RUN_KEY = "rp.cluster.lastRun";

	private final TaskExecutor logClusterExecutor;

	private final AnalyzerStatusCache analyzerStatusCache;
	private final LaunchPreparerService launchPreparerService;
	private final AnalyzerServiceClient analyzerServiceClient;

	private final CreateClusterHandler createClusterHandler;
	private final DeleteClusterHandler deleteClusterHandler;

	private final ItemAttributeRepository itemAttributeRepository;

	public ClusterGeneratorImpl(@Qualifier(value = "logClusterExecutor") TaskExecutor logClusterExecutor,
			AnalyzerStatusCache analyzerStatusCache, LaunchPreparerService launchPreparerService,
			AnalyzerServiceClient analyzerServiceClient, CreateClusterHandler createClusterHandler,
			DeleteClusterHandler deleteClusterHandler, ItemAttributeRepository itemAttributeRepository) {
		this.logClusterExecutor = logClusterExecutor;
		this.analyzerStatusCache = analyzerStatusCache;
		this.launchPreparerService = launchPreparerService;
		this.analyzerServiceClient = analyzerServiceClient;
		this.createClusterHandler = createClusterHandler;
		this.deleteClusterHandler = deleteClusterHandler;
		this.itemAttributeRepository = itemAttributeRepository;
	}

	@Override
	@Transactional
	public void generate(GenerateClustersConfig config) {

		expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services are deployed."
		);

		expect(analyzerStatusCache.containsLaunchId(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId()),
				Predicate.isEqual(false)
		).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Clusters creation is in progress.");

		analyzerStatusCache.analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId(), config.getProject());

		try {
			if (!config.isForUpdate()) {
				deleteClusterHandler.deleteLaunchClusters(config.getLaunchId());
			}
			logClusterExecutor.execute(() -> generateClusters(config));
		} catch (Exception ex) {
			analyzerStatusCache.analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId());
			LOGGER.error(ex.getMessage(), ex);
		}

	}

	private void generateClusters(GenerateClustersConfig config) {
		try {
			getGenerateRq(config).ifPresent(generateClustersRq -> {
				final ClusterData clusterData = analyzerServiceClient.generateClusters(generateClustersRq);
				createClusterHandler.create(clusterData);
			});
			saveLastRunAttribute(config);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		} finally {
			analyzerStatusCache.analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, config.getLaunchId());
		}
	}

	private Optional<GenerateClustersRq> getGenerateRq(GenerateClustersConfig config) {
		return launchPreparerService.prepare(config.getLaunchId(), config.getAnalyzerConfig()).map(indexLaunch -> {
			final GenerateClustersRq generateClustersRq = new GenerateClustersRq();
			generateClustersRq.setLaunch(indexLaunch);
			generateClustersRq.setProject(config.getProject());
			generateClustersRq.setCleanNumbers(config.isCleanNumbers());
			generateClustersRq.setForUpdate(config.isForUpdate());
			generateClustersRq.setNumberOfLogLines(config.getAnalyzerConfig().getNumberOfLogLines());
			return generateClustersRq;
		});
	}

	private void saveLastRunAttribute(GenerateClustersConfig config) {
		final String lastRunDate = String.valueOf(Instant.now().toEpochMilli());
		itemAttributeRepository.findByLaunchIdAndKeyAndSystem(config.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, false)
				.ifPresentOrElse(attr -> {
					attr.setValue(lastRunDate);
					itemAttributeRepository.save(attr);
				}, () -> itemAttributeRepository.saveByLaunchId(config.getLaunchId(), RP_CLUSTER_LAST_RUN_KEY, lastRunDate, false));
	}

}
