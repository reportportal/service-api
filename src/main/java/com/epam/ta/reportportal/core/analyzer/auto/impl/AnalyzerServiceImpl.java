/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;

import com.epam.reportportal.model.analyzer.IndexLaunch;
import com.epam.reportportal.model.project.AnalyzerConfig;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AnalyzerService}.
 *
 * <p>Auto-analysis is fire-and-forget: each partition is dispatched to all analyzers at once, and
 * the results are applied asynchronously by {@link AnalysisResultHandler} once they arrive on the
 * reply queue. The in-progress status is held only for the duration of the dispatch.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service
@Transactional
public class AnalyzerServiceImpl implements AnalyzerService {

  private static final Logger LOGGER = LogManager.getLogger(AnalyzerServiceImpl.class.getName());

  private final AnalyzerStatusCache analyzerStatusCache;

  private final LaunchPreparerService launchPreparerService;

  private final AnalyzerServiceClient analyzerServicesClient;

  private final TestItemRepository testItemRepository;

  private final LaunchRepository launchRepository;

  private final Integer itemsBatchSize;

  @Autowired
  public AnalyzerServiceImpl(
      @Value("${rp.environment.variable.item-analyze.batch-size}") Integer itemsBatchSize,
      AnalyzerStatusCache analyzerStatusCache, LaunchPreparerService launchPreparerService,
      AnalyzerServiceClient analyzerServicesClient, TestItemRepository testItemRepository,
      LaunchRepository launchRepository) {
    this.itemsBatchSize = itemsBatchSize;
    this.analyzerStatusCache = analyzerStatusCache;
    this.launchPreparerService = launchPreparerService;
    this.analyzerServicesClient = analyzerServicesClient;
    this.testItemRepository = testItemRepository;
    this.launchRepository = launchRepository;
  }

  @Override
  public boolean hasAnalyzers() {
    return analyzerServicesClient.hasClients();
  }

  @Override
  public void runAnalyzers(Launch launch, List<Long> testItemIds, AnalyzerConfig analyzerConfig) {
    try {
      analyzerStatusCache.analyzeStarted(AUTO_ANALYZER_KEY, launch.getId(), launch.getProjectId());
      Optional<Long> previousLaunchId = findPreviousLaunchId(launch, analyzerConfig);
      Iterables.partition(testItemIds, itemsBatchSize)
          .forEach(partition -> analyzeItemsPartition(launch, partition, analyzerConfig,
              previousLaunchId));
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    } finally {
      analyzerStatusCache.analyzeFinished(AUTO_ANALYZER_KEY, launch.getId());
    }
  }

  /**
   * Prepare and dispatch the number of provided test item ids to all analyzers asynchronously.
   *
   * @param launch         Launch
   * @param testItemIds    Item ids for analyzing
   * @param analyzerConfig Analyzer config
   */
  private void analyzeItemsPartition(Launch launch, List<Long> testItemIds,
      AnalyzerConfig analyzerConfig, Optional<Long> previousLaunchId) {
    LOGGER.info("Start analysis of '{}' items for launch with id '{}'", testItemIds.size(),
        launch.getId());
    List<TestItem> toAnalyze = testItemRepository.findAllById(testItemIds);
    Optional<IndexLaunch> rqLaunch = launchPreparerService.prepare(launch, toAnalyze,
        analyzerConfig);
    rqLaunch.ifPresent(rq -> {
      previousLaunchId.ifPresent(rq::setPreviousLaunchId);
      analyzerServicesClient.analyze(rq);
    });
  }

  /**
   * @param launch         Analyzed launch
   * @param analyzerConfig Current analyzer config
   * @return Id of previous launch. Required only for PREVIOUS_LAUNCH option.
   */
  private Optional<Long> findPreviousLaunchId(Launch launch, AnalyzerConfig analyzerConfig) {
    if (analyzerConfig.getAnalyzerMode().equals(AnalyzeMode.PREVIOUS_LAUNCH.getValue())) {
      return launchRepository.findPreviousLaunchId(launch);
    }
    return Optional.empty();
  }
}
