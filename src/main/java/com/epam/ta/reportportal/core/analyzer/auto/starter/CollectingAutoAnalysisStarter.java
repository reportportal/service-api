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

package com.epam.ta.reportportal.core.analyzer.auto.starter;

import static java.util.stream.Collectors.toList;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.analyzer.config.StartLaunchAutoAnalysisConfig;
import com.epam.ta.reportportal.core.launch.GetLaunchHandler;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class CollectingAutoAnalysisStarter implements LaunchAutoAnalysisStarter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectingAutoAnalysisStarter.class);

  private final GetLaunchHandler getLaunchHandler;
  private final AnalyzeCollectorFactory analyzeCollectorFactory;
  private final AnalyzerService analyzerService;
  private final LogIndexer logIndexer;

  public CollectingAutoAnalysisStarter(GetLaunchHandler getLaunchHandler,
      AnalyzeCollectorFactory analyzeCollectorFactory,
      AnalyzerService analyzerService, LogIndexer logIndexer) {
    this.getLaunchHandler = getLaunchHandler;
    this.analyzeCollectorFactory = analyzeCollectorFactory;
    this.analyzerService = analyzerService;
    this.logIndexer = logIndexer;
  }

  @Override
  @Transactional
  public void start(StartLaunchAutoAnalysisConfig config) {
    final Launch launch = getLaunchHandler.get(config.getLaunchId());

    final List<Long> itemIds = collectItemsByModes(launch, config.getAnalyzeItemsModes(),
        config.getUser());

    analyzerService.runAnalyzers(launch, itemIds, config.getAnalyzerConfig());
    logIndexer.indexItemsLogs(launch.getProjectId(), launch.getId(), itemIds,
        config.getAnalyzerConfig());
  }

  /**
   * Collect item ids for analyzer according to provided analyzer configuration.
   *
   * @return List of {@link TestItem#getItemId()} to analyze
   * @see AnalyzeItemsMode
   * @see AnalyzeCollectorFactory
   * @see AnalyzeItemsCollector
   */
  private List<Long> collectItemsByModes(Launch launch, Set<AnalyzeItemsMode> analyzeItemsModes,
      ReportPortalUser user) {
    return analyzeItemsModes.stream().flatMap(it -> {
      List<Long> itemIds = analyzeCollectorFactory.getCollector(it)
          .collectItems(launch.getProjectId(), launch.getId(), user);
      LOGGER.debug("Item itemIds collected by '{}' mode: {}", it, itemIds);
      return itemIds.stream();
    }).distinct().collect(toList());
  }
}
