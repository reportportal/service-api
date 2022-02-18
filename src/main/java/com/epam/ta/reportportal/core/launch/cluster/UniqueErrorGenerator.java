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

import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.launch.cluster.config.ClusterEntityContext;
import com.epam.ta.reportportal.core.launch.cluster.config.GenerateClustersConfig;
import com.epam.ta.reportportal.pipeline.PipelineConstructor;
import com.epam.ta.reportportal.pipeline.PipelinePart;
import com.epam.ta.reportportal.pipeline.TransactionalPipeline;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class UniqueErrorGenerator implements ClusterGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniqueErrorGeneratorAsync.class);

	private final AnalyzerStatusCache analyzerStatusCache;

	private final PipelineConstructor<GenerateClustersConfig> generateClustersPipelineConstructor;
	private final TransactionalPipeline transactionalPipeline;

	@Autowired
	public UniqueErrorGenerator(AnalyzerStatusCache analyzerStatusCache,
			PipelineConstructor<GenerateClustersConfig> generateClustersPipelineConstructor, TransactionalPipeline transactionalPipeline) {
		this.analyzerStatusCache = analyzerStatusCache;
		this.generateClustersPipelineConstructor = generateClustersPipelineConstructor;
		this.transactionalPipeline = transactionalPipeline;
	}

	@Override
	public void generate(GenerateClustersConfig config) {
		fillCache(config.getEntityContext());
		generateClusters(config);
	}

	protected void fillCache(ClusterEntityContext entityContext) {
		checkDuplicate(entityContext);
		analyzerStatusCache.analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY, entityContext.getLaunchId(), entityContext.getProjectId());
	}

	private void checkDuplicate(ClusterEntityContext entityContext) {
		expect(analyzerStatusCache.containsLaunchId(AnalyzerStatusCache.CLUSTER_KEY, entityContext.getLaunchId()),
				Predicate.isEqual(false)
		).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Clusters creation is in progress.");
	}

	protected void generateClusters(GenerateClustersConfig config) {
		try {
			final List<PipelinePart> pipelineParts = generateClustersPipelineConstructor.construct(config);
			transactionalPipeline.run(pipelineParts);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		} finally {
			cleanCache(config.getEntityContext());
		}
	}

	protected void cleanCache(ClusterEntityContext entityContext) {
		analyzerStatusCache.analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, entityContext.getLaunchId());
	}

}
