package com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HealthCheckTableContent implements Serializable {

  @JsonProperty(value = "attributeValue")
  private String attributeValue;

  @JsonProperty(value = "passingRate")
  private Double passingRate;

  @JsonProperty(value = "statistics")
  private Map<String, Integer> statistics;

  @JsonProperty(value = "customColumn")
  private List<String> customValues;

  public String getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }

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

  public List<String> getCustomValues() {
    return customValues;
  }

  public void setCustomValues(List<String> customValues) {
    this.customValues = customValues;
  }
}
