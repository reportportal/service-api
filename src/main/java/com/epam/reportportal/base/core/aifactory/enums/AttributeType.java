package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * How the UI places an attribute.
 */
@Schema(description = "MAIN - shown in the meta line / as the stage chip metric; TAG - shown as a chip (e.g. env:beta5); "
    + "HIDDEN - searchable but not displayed")
public enum AttributeType {
  MAIN,
  TAG,
  HIDDEN
}
