package com.epam.reportportal.base.infrastructure.persistence.dao.tms.enhanced;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_ATTRIBUTES;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_PRIORITY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_PLAN_ID;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_PLAN_TEST_CASE;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Condition;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.FilterCondition;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.filterable.TmsTestFolderFilterableRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderWithCountOfTestCases;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    var testCaseFilterParams = extractTestCaseFilterParams(filter);

    var result = folders
        .getContent()
        .stream()
        .map(folder -> {
          var testCasesCount = getTestCasesCount(folder.getId(), extractTestPlanId(filter),
              testCaseFilterParams);
          return new TmsTestFolderWithCountOfTestCases(folder, testCasesCount);
        })
        .filter(testFolder ->
            testCaseFilterParams == null || testFolder.getCountOfTestCases() != 0L
        )
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

  private TestCaseFilterParams extractTestCaseFilterParams(Filter filter) {
    String name = null;
    Condition nameCondition = null;
    String priority = null;
    Condition priorityCondition = null;
    List<Long> attributes = null;
    Condition attributesCondition = null;

    for (var condition : filter.getFilterConditions()) {
      if (condition instanceof FilterCondition fc) {
        if (CRITERIA_TMS_TEST_FOLDER_TEST_CASE_NAME.equals(fc.getSearchCriteria())) {
          name = fc.getValue();
          nameCondition = fc.getCondition();
        } else if (CRITERIA_TMS_TEST_FOLDER_TEST_CASE_PRIORITY.equals(fc.getSearchCriteria())) {
          priority = fc.getValue();
          priorityCondition = fc.getCondition();
        } else if (CRITERIA_TMS_TEST_FOLDER_TEST_CASE_ATTRIBUTES.equals(fc.getSearchCriteria())) {
          if (fc.getValue() != null && !fc.getValue().isEmpty()) {
            attributes = Arrays.stream(fc.getValue().split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
            attributesCondition = fc.getCondition();
          }
        }
      }
    }
    if ((name != null && nameCondition != null)
        || (priority != null && priorityCondition != null)
        || (attributes != null &&  attributesCondition != null)) {
      return new TestCaseFilterParams(name, nameCondition, priority, priorityCondition, attributes,
          attributesCondition);
    } else {
      return null;
    }
  }

  private long getTestCasesCount(Long folderId, Long testPlanId, TestCaseFilterParams testCaseFilterParams) {
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

    if (testCaseFilterParams != null) {
      if (testCaseFilterParams.name() != null) {
        if (testCaseFilterParams.nameCondition() == Condition.CONTAINS) {
          query = query.and(TMS_TEST_CASE.NAME.containsIgnoreCase(testCaseFilterParams.name()));
        } else if (testCaseFilterParams.nameCondition() == Condition.EQUALS) {
          query = query.and(TMS_TEST_CASE.NAME.eq(testCaseFilterParams.name()));
        }
      }
      if (testCaseFilterParams.priority() != null) {
        if (testCaseFilterParams.priorityCondition() == Condition.EQUALS) {
          query = query.and(TMS_TEST_CASE.PRIORITY.eq(testCaseFilterParams.priority()));
        } else if (testCaseFilterParams.priorityCondition() == Condition.IN) {
          query = query.and(TMS_TEST_CASE.PRIORITY.in(testCaseFilterParams.priority().split(",")));
        }
      }
      if (testCaseFilterParams.attributes() != null && !testCaseFilterParams.attributes().isEmpty()) {
        if (testCaseFilterParams.attributesCondition() == Condition.HAS) {
          query = query.and(TMS_TEST_CASE.ID.in(
              dsl.select(TMS_TEST_CASE_ATTRIBUTE.TEST_CASE_ID)
                  .from(TMS_TEST_CASE_ATTRIBUTE)
                  .groupBy(TMS_TEST_CASE_ATTRIBUTE.TEST_CASE_ID)
                  .having(DSL.condition("array_agg(distinct {0}) @> {1}",
                      TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID,
                      DSL.val(testCaseFilterParams.attributes().toArray(new Long[0]))))
          ));
        }
      }
    }

    return query.fetchOne(0, Long.class);
  }

  private record TestCaseFilterParams(
      String name,
      Condition nameCondition,
      String priority,
      Condition priorityCondition,
      List<Long> attributes,
      Condition attributesCondition
  ) {
  }
}
