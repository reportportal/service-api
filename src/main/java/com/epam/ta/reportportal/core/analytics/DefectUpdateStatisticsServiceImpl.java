/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.core.analytics;

import static com.epam.reportportal.rules.exception.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.core.analytics.AnalyticsObjectType.DEFECT_UPDATE_STATISTICS;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;

import com.epam.reportportal.model.project.AnalyzerConfig;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.dao.AnalyticsDataRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.ServerSettingsRepository;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.analytics.AnalyticsData;
import com.epam.ta.reportportal.entity.project.Project;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * DefectUpdateStatistics is an implementation of the AnalyticsStrategy interface. This class is
 * responsible for persisting the defect update statistics.
 */
@Component
public class DefectUpdateStatisticsServiceImpl implements DefectUpdateStatisticsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      DefectUpdateStatisticsServiceImpl.class);
  public static final String SERVER_ANALYTICS_ALL = "server.analytics.all";

  private final String buildVersion;

  private final AnalyticsDataRepository analyticsDataRepository;
  private final AnalyzerServiceClient analyzerServicesClient;
  private final ProjectRepository projectRepository;
  private final ServerSettingsRepository serverSettingsRepository;


  @Autowired
  public DefectUpdateStatisticsServiceImpl(
      @Value("${info.build.version:unknown}") String buildVersion,
      AnalyticsDataRepository analyticsDataRepository,
      AnalyzerServiceClient analyzerServicesClient,
      ProjectRepository projectRepository, ServerSettingsRepository serverSettingsRepository) {
    this.buildVersion = buildVersion;
    this.analyzerServicesClient = analyzerServicesClient;
    this.analyticsDataRepository = analyticsDataRepository;
    this.projectRepository = projectRepository;
    this.serverSettingsRepository = serverSettingsRepository;
  }


  /**
   * This method saves analyzed defect statistics into the database.
   *
   * @param amountToAnalyze    The amount of items to analyze.
   * @param analyzedAmount     The amount of items that have been analyzed.
   * @param userAnalyzedAmount The amount of items that have been analyzed by the user.
   * @param projectId          The ID of the project.
   */
  @Override
  public void saveAnalyzedDefectStatistics(int amountToAnalyze, int analyzedAmount,
      int userAnalyzedAmount, int skipped, Long projectId) {
    if (isAnalyticsGatheringAllowed()) {
      var map = getMapWithCommonParameters(projectId);
      map.put("sentToAnalyze", amountToAnalyze);
      map.put("analyzed", analyzedAmount);
      map.put("userAnalyzed", userAnalyzedAmount);
      map.put("skipped", skipped);

      AnalyticsData ad = new AnalyticsData();
      ad.setType(DEFECT_UPDATE_STATISTICS.name());
      ad.setCreatedAt(Instant.now());
      ad.setMetadata(new Metadata(map));
      analyticsDataRepository.save(ad);
    }

  }

  @Override
  public void saveAutoAnalyzedDefectStatistics(int amountToAnalyze, int analyzedAmount, int skipped,
      Long projectId) {
    this.saveAnalyzedDefectStatistics(amountToAnalyze, analyzedAmount, 0, skipped,
        projectId);
  }

  @Override
  public void saveUserAnalyzedDefectStatistics(int userAnalyzed, Long projectId) {
    this.saveAnalyzedDefectStatistics(userAnalyzed, 0, userAnalyzed, 0, projectId);
  }

  private boolean isAnalyticsGatheringAllowed() {
    return serverSettingsRepository.selectServerSettings()
        .stream()
        .filter(setting -> setting.getKey().equals(SERVER_ANALYTICS_ALL))
        .findFirst()
        .map(serverSettings -> Boolean.parseBoolean(serverSettings.getValue()))
        .orElse(false);
  }

  private Map<String, Object> getMapWithCommonParameters(Long projectId) {
    Map<String, Object> map = new HashMap<>();
    map.put("autoAnalysisOn", getIsAutoAnalyzerEnabled(projectId));
    try {
      map.put("analyzerEnabled", analyzerServicesClient.hasClients());
    } catch (ReportPortalException rpe) {
      LOGGER.debug("Analyzer is not enabled", rpe);
    }
    map.put("version", buildVersion);
    return map;
  }

  private boolean getIsAutoAnalyzerEnabled(Long projectId) {
    Project project = projectRepository.findById(projectId).orElseThrow(
        () -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));
    AnalyzerConfig analyzerConfig = getAnalyzerConfig(project);
    return analyzerConfig.getIsAutoAnalyzerEnabled();
  }

}
