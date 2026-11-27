package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * How an attribute value (or a result table cell) is to be interpreted and formatted. The value itself
 * is always carried as text.
 */
@Schema(description = "TEXT, NUMBER, SCORE (a rating, higher usually better), MONEY (USD), DURATION (milliseconds), "
    + "DATE (ISO-8601 instant), LINK (the url field is the target), STATUS (a status text, coloured like stage statuses)")
public enum AttributeValueType {
  TEXT,
  NUMBER,
  SCORE,
  MONEY,
  DURATION,
  DATE,
  LINK,
  STATUS;

  /**
   * Whether values of this type can be subtracted in a comparison.
   */
  public boolean isNumeric() {
    return this == NUMBER || this == SCORE || this == MONEY || this == DURATION;
  }
}
