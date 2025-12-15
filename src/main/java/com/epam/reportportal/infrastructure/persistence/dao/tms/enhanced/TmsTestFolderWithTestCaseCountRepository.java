package com.epam.reportportal.infrastructure.persistence.dao.tms.enhanced;

import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_PROJECT_ID;
import static com.epam.reportportal.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_PLAN_ID;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;
import static com.epam.reportportal.infrastructure.persistence.jooq.Tables.TMS_TEST_PLAN_TEST_CASE;

import com.epam.reportportal.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.infrastructure.persistence.dao.tms.filterable.TmsTestFolderFilterableRepository;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestFolderWithCountOfTestCases;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TmsTestFolderWithTestCaseCountRepository {

  private final DSLContext dsl;
  private final TmsTestFolderFilterableRepository filterableRepository;

  public Page<TmsTestFolderWithCountOfTestCases> findAllByProjectIdAndFilterWithCountOfTestCases(
      long projectId, Filter filter, Pageable pageable) {

    var enhancedFilter = enhanceFilterWithProjectId(filter, projectId);

    var folders = filterableRepository.findByFilter(enhancedFilter, pageable);

    var result = folders
        .getContent()
        .stream()
        .map(folder -> {
          var testCasesCount = getTestCasesCount(folder.getId(), extractTestPlanId(filter));
          return new TmsTestFolderWithCountOfTestCases(folder, testCasesCount);
        })
        .collect(Collectors.toList());

    return new PageImpl<>(result, pageable, folders.getTotalElements());
  }

  private Filter enhanceFilterWithProjectId(Filter originalFilter, long projectId) {
    var conditions = new ArrayList<>(originalFilter.getFilterConditions());

    var hasProjectIdFilter = conditions
        .stream()
        .anyMatch(condition -> condition instanceof FilterCondition &&
            ((FilterCondition) condition).getSearchCriteria()
                .equals(CRITERIA_TMS_TEST_FOLDER_PROJECT_ID));

    if (!hasProjectIdFilter) {
      conditions.add(new FilterCondition(
          Condition.EQUALS,
          false,
          String.valueOf(projectId),
          CRITERIA_TMS_TEST_FOLDER_PROJECT_ID
      ));
    }

    return new Filter(TmsTestFolder.class, conditions);
  }

  private Long extractTestPlanId(Filter filter) {
    return filter
        .getFilterConditions()
        .stream()
        .filter(condition -> condition instanceof FilterCondition)
        .map(condition -> (FilterCondition) condition)
        .filter(fc -> CRITERIA_TMS_TEST_FOLDER_TEST_PLAN_ID.equals(fc.getSearchCriteria()))
        .findFirst()
        .map(fc -> Long.valueOf(fc.getValue()))
        .orElse(null);
  }

  private long getTestCasesCount(Long folderId, Long testPlanId) {
    var query = dsl
        .selectCount()
        .from(TMS_TEST_CASE)
        .where(TMS_TEST_CASE.TEST_FOLDER_ID.eq(folderId));

    if (testPlanId != null) {
      query = query.and(DSL.exists(
          dsl.selectOne()
              .from(TMS_TEST_PLAN_TEST_CASE)
              .where(TMS_TEST_PLAN_TEST_CASE.TEST_CASE_ID.eq(TMS_TEST_CASE.ID))
              .and(TMS_TEST_PLAN_TEST_CASE.TEST_PLAN_ID.eq(testPlanId))
      ));
    }

    return query.fetchOne(0, Long.class);
  }
}
