package com.epam.reportportal.base.infrastructure.persistence.entity.widget;

import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public enum WidgetState {

  CREATED("created"),

  RENDERING("rendering"),

  READY("ready"),

  FAILED("failed");

  private final String value;

  WidgetState(String value) {
    this.value = value;
  }

  public static Optional<WidgetState> findByName(@Nullable String name) {
    return Arrays.stream(WidgetState.values())
        .filter(state -> state.getValue().equalsIgnoreCase(name)).findAny();
  }

  public String getValue() {
    return value;
  }
}
