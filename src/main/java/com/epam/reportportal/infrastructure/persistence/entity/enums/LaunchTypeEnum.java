package com.epam.reportportal.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Launch type enum.
 */
public enum LaunchTypeEnum {

  AUTOMATION("AUTOMATION"),
  MANUAL("MANUAL");

  private final String value;

  LaunchTypeEnum(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static Optional<LaunchTypeEnum> fromValue(String value) {
    return Arrays.stream(values())
        .filter(type -> type.value.equalsIgnoreCase(value))
        .findFirst();
  }

  public boolean isManual() {
    return this == MANUAL;
  }

  public boolean isAutomation() {
    return this == AUTOMATION;
  }
}
