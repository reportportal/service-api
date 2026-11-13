package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

/**
 * System integration type names (email, BTS) that are reserved and always available.
 *
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Getter
public enum ReservedIntegrationTypeEnum {

  EMAIL("email");

  private final String name;

  ReservedIntegrationTypeEnum(String name) {
    this.name = name;
  }

  public static Optional<ReservedIntegrationTypeEnum> fromName(String name) {
    return Arrays.stream(values()).filter(it -> it.getName().equalsIgnoreCase(name)).findAny();
  }

}
