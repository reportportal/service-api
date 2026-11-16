package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

/**
 * Enumeration of current feature flags.
 *
 * @author <a href="mailto:ivan_kustau@epam.com">Ivan Kustau</a>
 */
@Getter
public enum FeatureFlag {
  SINGLE_BUCKET("singleBucket"),
  DEFAULT_LDAP_ENCODER("defaultLdapEncoder");

  private final String name;

  FeatureFlag(String name) {
    this.name = name;
  }

  /**
   * Returns {@link Optional} of {@link FeatureFlag} by string.
   *
   * @param name Name of feature flag
   * @return {@link Optional} of {@link FeatureFlag} by string
   */
  public static Optional<FeatureFlag> fromString(String name) {
    return Optional.ofNullable(name).flatMap(
        str -> Arrays.stream(values()).filter(it -> it.name.equalsIgnoreCase(str)).findAny());

  }
}
