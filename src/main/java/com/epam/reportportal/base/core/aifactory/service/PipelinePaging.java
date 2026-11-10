package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.util.OffsetRequest;
import org.springframework.data.domain.Sort;

/**
 * Paging guard shared by the pipeline list endpoints: {@code OffsetArgumentResolver} accepts any
 * {@code limit}, so a caller could otherwise request an unbounded page.
 */
final class PipelinePaging {

  static final int MAX_LIMIT = 200;

  private PipelinePaging() {
  }

  /**
   * Caps the limit and fixes the ordering: the list endpoints have a defined order the caller
   * cannot change.
   */
  static OffsetRequest capLimit(OffsetRequest request, Sort sort) {
    return new OffsetRequest(request.getOffset(), Math.min(request.getPageSize(), MAX_LIMIT), sort);
  }

  static OffsetRequest capLimit(OffsetRequest request) {
    if (request.getPageSize() <= MAX_LIMIT) {
      return request;
    }
    return new OffsetRequest(request.getOffset(), MAX_LIMIT, request.getSort());
  }
}
