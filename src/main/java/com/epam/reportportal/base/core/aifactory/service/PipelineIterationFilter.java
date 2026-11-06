package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import java.util.Map;
import java.util.Set;

/**
 * Optional narrowing of the iteration list. Every field is optional; blank values are ignored.
 *
 * @param search     case-insensitive substring over the iteration's attribute values, or exact iteration
 *                   number (a leading {@code #} is ignored)
 * @param statuses   iteration status category is any of these
 * @param attributes exact attribute filters ({@code key -> value}); every one must match
 */
public record PipelineIterationFilter(String search, Set<StatusCategory> statuses, Map<String, String> attributes) {

  public static PipelineIterationFilter none() {
    return new PipelineIterationFilter(null, null, null);
  }
}
