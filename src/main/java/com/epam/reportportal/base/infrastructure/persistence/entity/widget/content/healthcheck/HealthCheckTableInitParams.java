package com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck;

import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HealthCheckTableInitParams {

  private final String viewName;
  private final List<String> attributeKeys;

  @Nullable
  private String customKey;

  private HealthCheckTableInitParams(String viewName, List<String> attributeKeys) {
    this.viewName = viewName;
    this.attributeKeys = attributeKeys;
  }

  private HealthCheckTableInitParams(String viewName, List<String> attributeKeys,
      @Nullable String customKey) {
    this.viewName = viewName;
    this.attributeKeys = attributeKeys;
    this.customKey = customKey;
  }

  public static HealthCheckTableInitParams of(String viewName, List<String> attributeKeys) {
    return new HealthCheckTableInitParams(viewName, attributeKeys);
  }

  public static HealthCheckTableInitParams of(String viewName, List<String> attributeKeys,
      @Nullable String customKey) {
    return new HealthCheckTableInitParams(viewName, attributeKeys, customKey);
  }

  public String getViewName() {
    return viewName;
  }

  public List<String> getAttributeKeys() {
    return attributeKeys;
  }

  @Nullable
  public String getCustomKey() {
    return customKey;
  }

  public void setCustomKey(@Nullable String customKey) {
    this.customKey = customKey;
  }
}
