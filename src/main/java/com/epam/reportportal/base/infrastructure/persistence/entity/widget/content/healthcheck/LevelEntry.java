package com.epam.reportportal.base.infrastructure.persistence.entity.widget.content.healthcheck;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class LevelEntry {

  private final String key;
  private final String value;

  private LevelEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public static LevelEntry of(String key, String value) {
    return new LevelEntry(key, value);
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
