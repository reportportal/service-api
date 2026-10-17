package com.epam.reportportal.base.core.aifactory.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Where the UI puts an attribute. Any number of attributes of each kind is allowed.
 * <p>
 * Example on an iteration: {@code requirement}, {@code trigger}, {@code model}, {@code suiteScore} and
 * {@code cost} are {@link #META} (the line under the title), {@code env:beta5} is a {@link #TAG} (a chip),
 * {@code requirementTitle} is {@link #NONE} (returned for the UI to pick by key, drawn nowhere by itself).
 */
@Schema(description = "META - shown on the summary line of its owner (an iteration's meta line, a stage card), "
    + "ordered by 'order'; TAG - shown as a chip (e.g. env:beta5); NONE - returned and searchable but not drawn "
    + "automatically, the UI may read it by key")
public enum AttributeType {
  META,
  TAG,
  NONE
}
