package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import java.util.Optional;

/**
 * How the agent tells ReportPortal that an iteration it reports is the answer to a fix round.
 * <p>
 * Example: the fix job received {@code push_id=7}. It reports its iteration as a re-run of the one that produced
 * the case, with the iteration attribute {@code {"key": "fixPushId", "value": "7", "type": "NONE"}}. From it
 * ReportPortal knows which round the iteration belongs to, ends the round when the work fails, and labels the
 * iteration "Fix round 1" in the list.
 */
final class FixRoundIterationSupport {

  static final String FIX_PUSH_ID_ATTRIBUTE_KEY = "fixPushId";

  private FixRoundIterationSupport() {
  }

  /**
   * The id of the fix round the iteration answers, when the agent reported one.
   */
  static Optional<Long> fixPushId(PipelineIteration iteration) {
    if (iteration == null || iteration.getAttributes() == null || iteration.getAttributes().getItems() == null) {
      return Optional.empty();
    }
    return iteration.getAttributes().getItems().stream()
        .filter(attribute -> FIX_PUSH_ID_ATTRIBUTE_KEY.equals(attribute.getKey()))
        .map(PipelineAttribute::getValue)
        .findFirst()
        .flatMap(FixRoundIterationSupport::parseId);
  }

  private static Optional<Long> parseId(String value) {
    try {
      return Optional.of(Long.parseLong(value.strip()));
    } catch (NumberFormatException | NullPointerException e) {
      return Optional.empty();
    }
  }
}
