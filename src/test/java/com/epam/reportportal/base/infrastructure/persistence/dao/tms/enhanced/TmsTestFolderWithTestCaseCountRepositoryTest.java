package com.epam.reportportal.base.infrastructure.persistence.dao.tms.enhanced;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Queryable;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsTestFolderFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class TmsTestFolderWithTestCaseCountRepositoryTest {

  private static final long PROJECT_ID = 1L;
  private static final long FOREIGN_PLAN_ID = 77L;

  private final DSLContext dsl = mock(DSLContext.class, RETURNS_DEEP_STUBS);
  private final TmsTestFolderFilterableRepository filterableRepository =
      mock(TmsTestFolderFilterableRepository.class);
  private final TmsTestFolderWithTestCaseCountRepository repository =
      new TmsTestFolderWithTestCaseCountRepository(dsl, filterableRepository);
  private final Pageable pageable = PageRequest.of(0, 10);

  private static Filter planFilter(Condition condition, boolean negative) {
    return new Filter(TmsTestFolder.class, List.of(
        new FilterCondition(condition, negative, String.valueOf(FOREIGN_PLAN_ID), "testPlanId")));
  }

  @Test
  void equalsForeignTestPlan_ReturnsEmptyPageWithoutQueryingFolders() {
    when(dsl.fetchExists(any(Select.class))).thenReturn(false);

    var page = repository.findAllByProjectIdAndFilterWithCountOfTestCases(PROJECT_ID,
        planFilter(Condition.EQUALS, false), pageable);

    assertTrue(page.isEmpty());
    verify(filterableRepository, never()).findByFilter(any(Queryable.class), any(Pageable.class));
  }

  @Test
  void exclusionOfForeignTestPlan_StillQueriesProjectFolders() {
    when(filterableRepository.findByFilter(any(Queryable.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    for (var filter : List.of(planFilter(Condition.NOT_EQUALS, false),
        planFilter(Condition.EQUALS, true))) {
      repository.findAllByProjectIdAndFilterWithCountOfTestCases(PROJECT_ID, filter, pageable);
    }

    verify(dsl, never()).fetchExists(any(Select.class));
    verify(filterableRepository, times(2))
        .findByFilter(any(Queryable.class), any(Pageable.class));
  }
}
