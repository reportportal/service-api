package com.epam.reportportal.infrastructure.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.epam.reportportal.ws.BaseMvcTest;
import com.epam.reportportal.infrastructure.persistence.entity.Metadata;
import com.epam.reportportal.infrastructure.persistence.entity.analytics.AnalyticsData;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AnalyticsDataRepositoryTest extends BaseMvcTest {

  @Autowired
  private AnalyticsDataRepository analyticsDataRepository;


  @Test
  void persistAnalyticsDataSuccess() {
    AnalyticsData analyticsData = new AnalyticsData();
    analyticsData.setType("ANALYZER_MANUAL_START");

    Map<String, Object> map = new HashMap<>();
    map.put("version", 5.11);
    analyticsData.setMetadata(new Metadata(map));
    analyticsData.setCreatedAt(Instant.now());
    AnalyticsData asd = analyticsDataRepository.save(analyticsData);

    AnalyticsData persistedRecord = analyticsDataRepository.getById(asd.getId());

    assertNotNull(persistedRecord.getCreatedAt());
    assertNotNull(persistedRecord.getType());
  }
}
