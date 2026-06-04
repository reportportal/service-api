package com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TmsEntityDisplayIdType {
  TEST_CASE("TEST_CASE", "TC"),
  TEST_PLAN("TEST_PLAN", "TP"),
  MILESTONE("MILESTONE", "MS"),
  MANUAL_LAUNCH("MANUAL_LAUNCH", "ML");

  private final String entityType;
  private final String prefix;
}
