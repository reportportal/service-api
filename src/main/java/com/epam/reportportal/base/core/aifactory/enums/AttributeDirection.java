package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Which way a numeric attribute improves, used to colour a delta when two iterations are compared.
 */
@Schema(description = "HIGHER_BETTER - an increase is an improvement (score); LOWER_BETTER - a decrease is an "
    + "improvement (cost, duration); NONE - the delta is informational")
public enum AttributeDirection {
  HIGHER_BETTER,
  LOWER_BETTER,
  NONE
}
