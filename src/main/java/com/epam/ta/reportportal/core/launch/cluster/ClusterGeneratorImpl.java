package com.epam.ta.reportportal.core.launch.cluster;

import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.ClusterData;
import com.epam.ta.reportportal.core.analyzer.auto.client.model.cluster.GenerateClustersRq;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

@Service
public class ClusterGeneratorImpl implements ClusterGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterGeneratorImpl.class);

	private final TaskExecutor logClusterExecutor;

	private final AnalyzerStatusCache analyzerStatusCache;
	private final AnalyzerServiceClient analyzerServiceClient;

	private final CreateClusterHandler createClusterHandler;
	private final DeleteClusterHandler deleteClusterHandler;

	public ClusterGeneratorImpl(@Qualifier(value = "logClusterExecutor") TaskExecutor logClusterExecutor,
			AnalyzerStatusCache analyzerStatusCache, AnalyzerServiceClient analyzerServiceClient, CreateClusterHandler createClusterHandler,
			DeleteClusterHandler deleteClusterHandler) {
		this.logClusterExecutor = logClusterExecutor;
		this.analyzerStatusCache = analyzerStatusCache;
		this.analyzerServiceClient = analyzerServiceClient;
		this.createClusterHandler = createClusterHandler;
		this.deleteClusterHandler = deleteClusterHandler;
	}

	@Override
	@Transactional
	public void generate(GenerateClustersRq generateClustersRq) {

		expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer services are deployed."
		);

		expect(analyzerStatusCache.containsLaunchId(AnalyzerStatusCache.CLUSTER_KEY, generateClustersRq.getLaunchId()),
				Predicate.isEqual(false)
		).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Clusters creation is in progress.");

		analyzerStatusCache.analyzeStarted(AnalyzerStatusCache.CLUSTER_KEY,
				generateClustersRq.getLaunchId(),
				generateClustersRq.getProject()
		);

		try {
			if (!generateClustersRq.isForUpdate()) {
				deleteClusterHandler.deleteLaunchClusters(generateClustersRq.getLaunchId());
			}
			logClusterExecutor.execute(() -> generateClusters(generateClustersRq));
		} catch (Exception ex) {
			analyzerStatusCache.analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY, generateClustersRq.getLaunchId());
			LOGGER.error(ex.getMessage(), ex);
		}

	}

	private void generateClusters(GenerateClustersRq generateClustersRq) {
		try {
			final ClusterData clusterData = analyzerServiceClient.generateClusters(generateClustersRq);
			createClusterHandler.create(clusterData);
		} catch (Exception ex) {
			LOGGER.error(ex.getMessage(), ex);
		} finally {
			analyzerStatusCache.analyzeFinished(AnalyzerStatusCache.CLUSTER_KEY,
					generateClustersRq.getLaunchId()
			);
		}
	}

}
