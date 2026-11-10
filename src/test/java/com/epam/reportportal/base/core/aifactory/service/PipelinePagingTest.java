package com.epam.reportportal.base.core.aifactory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.util.OffsetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class PipelinePagingTest {

  private static final Sort SORT = Sort.by("name");

  @Test
  void capLimit_WhenWithinMax_ShouldKeepLimitAndOffset() {
    var request = PipelinePaging.capLimit(new OffsetRequest(10, 50, Sort.by("ignored")), SORT);

    assertEquals(50, request.getPageSize());
    assertEquals(10, request.getOffset());
  }

  @Test
  void capLimit_WhenOverMax_ShouldClampLimitAndKeepOffset() {
    var capped = PipelinePaging.capLimit(new OffsetRequest(30, 5000), SORT);

    assertEquals(PipelinePaging.MAX_LIMIT, capped.getPageSize());
    assertEquals(30, capped.getOffset());
  }

  @Test
  void capLimit_ShouldReplaceCallerSortWithTheGivenOne() {
    var capped = PipelinePaging.capLimit(new OffsetRequest(0, 10, Sort.by("ignored")), SORT);

    assertEquals(SORT, capped.getSort());
  }
}
