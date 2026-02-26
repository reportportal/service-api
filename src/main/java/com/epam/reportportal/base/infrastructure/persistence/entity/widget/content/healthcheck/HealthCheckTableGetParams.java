package com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.data.domain.Sort;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class HealthCheckTableGetParams {

  private final String viewName;
  private final String currentLevelKey;
  private final Sort sort;

  private final boolean includeCustomColumn;
  private final List<LevelEntry> previousLevels;

  private final Boolean excludeSkippedTests;

  private HealthCheckTableGetParams(String viewName, String currentLevelKey, Sort sort,
      boolean includeCustomColumn, Boolean excludeSkippedTests) {
    this.viewName = viewName;
    this.currentLevelKey = currentLevelKey;
    this.sort = sort;
    this.includeCustomColumn = includeCustomColumn;
    this.excludeSkippedTests = excludeSkippedTests;
    this.previousLevels = Lists.newArrayList();
  }

  private HealthCheckTableGetParams(String viewName, String currentLevelKey, Sort sort,
      boolean includeCustomColumn,
      List<LevelEntry> previousLevels, Boolean excludeSkippedTests) {
    this.viewName = viewName;
    this.currentLevelKey = currentLevelKey;
    this.sort = sort;
    this.includeCustomColumn = includeCustomColumn;
    this.previousLevels = previousLevels;
    this.excludeSkippedTests = excludeSkippedTests;
  }

  public static HealthCheckTableGetParams of(String viewName, String currentLevelKey, Sort sort,
      boolean includeCustomColumn, Boolean excludeSkippedTests) {
    return new HealthCheckTableGetParams(viewName, currentLevelKey, sort, includeCustomColumn,
        excludeSkippedTests);
  }

  public static HealthCheckTableGetParams of(String viewName, String currentLevelKey, Sort sort,
      boolean includeCustomColumn, List<LevelEntry> previousLevels, Boolean excludeSkippedTests) {
    return new HealthCheckTableGetParams(viewName, currentLevelKey, sort, includeCustomColumn,
        previousLevels, excludeSkippedTests);
  }

  public String getViewName() {
    return viewName;
  }

  public String getCurrentLevelKey() {
    return currentLevelKey;
  }

  public Sort getSort() {
    return sort;
  }

  public boolean isIncludeCustomColumn() {
    return includeCustomColumn;
  }

  public List<LevelEntry> getPreviousLevels() {
    return previousLevels;
  }

  public Boolean isExcludeSkippedTests() {
    return excludeSkippedTests;
  }
}
