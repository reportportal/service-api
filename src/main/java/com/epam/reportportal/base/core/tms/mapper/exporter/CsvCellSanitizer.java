package com.epam.reportportal.base.core.tms.mapper.exporter;

/**
 * Protects CSV exports against formula injection. Spreadsheet applications execute cells that
 * start with {@code =}, {@code +}, {@code -}, {@code @}, tab or carriage return as formulas, so
 * such user-controlled text is prefixed with a single quote to be shown as plain text.
 */
final class CsvCellSanitizer {

  private static final String FORMULA_PREFIX = "'";

  private CsvCellSanitizer() {
  }

  /**
   * Neutralizes a free-text value that could be interpreted as a spreadsheet formula.
   *
   * @param value Free-text value, may be {@code null}
   * @return The value prefixed with a single quote if it starts with a formula trigger character,
   * otherwise the value as is
   */
  static String sanitize(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    return switch (value.charAt(0)) {
      case '=', '+', '-', '@', '\t', '\r' -> FORMULA_PREFIX + value;
      default -> value;
    };
  }
}
