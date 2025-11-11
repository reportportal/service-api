package com.epam.reportportal.infrastructure.persistence.entity.widget.content.healthcheck;

import com.google.common.collect.Maps;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HealthCheckTableStatisticsContent {

  private Double passingRate;

  private Map<String, Integer> statistics = Maps.newHashMap();

  public Double getPassingRate() {
    return passingRate;
  }

  public void setPassingRate(Double passingRate) {
    this.passingRate = passingRate;
  }

  public Map<String, Integer> getStatistics() {
    return statistics;
  }

  public void setStatistics(Map<String, Integer> statistics) {
    this.statistics = statistics;
  }
}
