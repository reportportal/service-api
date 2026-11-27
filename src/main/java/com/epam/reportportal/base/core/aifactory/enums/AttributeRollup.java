package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * How an attribute reported on stages is summed up into the iteration.
 */
@Schema(description = "SUM - the iteration gets this attribute as the sum of its stages' values, unless the pipeline "
    + "reported it on the iteration itself")
public enum AttributeRollup {
  SUM
}
