package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;


/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
class FilterConditionTest {

  @Test
  public void testInBuilder() {
    FilterCondition condition = FilterCondition.builder()
        .in("criteria", Lists.newArrayList(1, 2, 3)).build();
    assertEquals("criteria", condition.getSearchCriteria());
    assertEquals(Condition.IN, condition.getCondition());
    assertEquals("1,2,3", condition.getValue());
  }

  @Test
  public void testEqBuilder() {
    FilterCondition condition = FilterCondition.builder().eq("criteria", "value").build();
    assertEquals("criteria", condition.getSearchCriteria());
    assertEquals(Condition.EQUALS, condition.getCondition());
    assertEquals("value", condition.getValue());
  }

  @Test
  void testCaseIdValueCastsToHash() {
    CriteriaHolder criteriaHolder = FilterTarget.TEST_ITEM_TARGET
        .getCriteriaByFilter(CRITERIA_TEST_CASE_ID)
        .orElseThrow();

    assertEquals("testCaseId".hashCode(), criteriaHolder.castValue("testCaseId"));
  }

  @Test
  void testCaseIdAllowsOnlyEqualsConditions() {
    CriteriaHolder criteriaHolder = FilterTarget.TEST_ITEM_TARGET
        .getCriteriaByFilter(CRITERIA_TEST_CASE_ID)
        .orElseThrow();

    criteriaHolder.validateCondition(Condition.EQUALS, ErrorType.INCORRECT_FILTER_PARAMETERS);
    criteriaHolder.validateCondition(Condition.NOT_EQUALS, ErrorType.INCORRECT_FILTER_PARAMETERS);
    assertThrows(ReportPortalException.class,
        () -> criteriaHolder.validateCondition(Condition.CONTAINS,
            ErrorType.INCORRECT_FILTER_PARAMETERS));
  }

}
