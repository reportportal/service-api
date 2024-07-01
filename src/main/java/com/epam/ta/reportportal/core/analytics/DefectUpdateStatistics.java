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

import static com.epam.ta.reportportal.core.analytics.AnalyticsObjectType.DEFECT_UPDATE_STATISTICS;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.dao.AnalyticsDataRepository;
import com.epam.ta.reportportal.entity.Metadata;
import com.epam.ta.reportportal.entity.analytics.AnalyticsData;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class DefectUpdateStatistics implements AnalyticsStrategy {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefectUpdateStatistics.class);

  @Value("${info.build.version:unknown}")
  private String buildVersion;

  private final AnalyticsDataRepository analyticsDataRepository;
  private final AnalyzerServiceClient analyzerServicesClient;

  @Autowired
  public DefectUpdateStatistics(AnalyticsDataRepository analyticsDataRepository,
      AnalyzerServiceClient analyzerServicesClient) {
    this.analyzerServicesClient = analyzerServicesClient;
    this.analyticsDataRepository = analyticsDataRepository;
  }

  @Override
  public void persistAnalyticsData(Map<String, Object> map) {
    AnalyticsData ad = new AnalyticsData();
    ad.setType(DEFECT_UPDATE_STATISTICS.name());
    ad.setCreatedAt(Instant.now());

    try {
      map.put("analyzerEnabled", analyzerServicesClient.hasClients());
    } catch (ReportPortalException rpe) {
      LOGGER.debug("Analyzer is not enabled", rpe);
    }
    map.put("version", buildVersion);
    ad.setMetadata(new Metadata(map));
    analyticsDataRepository.save(ad);
  }

  @Override
  public AnalyticsObjectType getStrategyName() {
    return DEFECT_UPDATE_STATISTICS;
  }
}
