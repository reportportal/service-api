package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_RETENTION_POLICY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_DISPLAY_ID;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JRetentionPolicyEnum;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.google.common.collect.Lists;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
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
  void tmsTestCaseDisplayIdIsSupported() {
    CriteriaHolder criteriaHolder = FilterTarget.TMS_TEST_CASE_TARGET
        .getCriteriaByFilter(CRITERIA_TMS_TEST_CASE_DISPLAY_ID)
        .orElseThrow();

    assertEquals(CRITERIA_TMS_TEST_CASE_DISPLAY_ID, criteriaHolder.getFilterCriteria());
    assertEquals(String.class, criteriaHolder.getDataType());
    assertEquals(TMS_TEST_CASE.DISPLAY_ID.getQualifiedName().toString(), criteriaHolder.getQueryCriteria());
  }

  @Test
  void retentionPolicyFilterTargetsLaunchColumn() {
    CriteriaHolder criteriaHolder = FilterTarget.LAUNCH_TARGET
        .getCriteriaByFilter(CRITERIA_RETENTION_POLICY)
        .orElseThrow();

    assertEquals(CRITERIA_RETENTION_POLICY, criteriaHolder.getFilterCriteria());
    assertEquals(JRetentionPolicyEnum.class, criteriaHolder.getDataType());
    assertEquals(LAUNCH.RETENTION_POLICY.getQualifiedName().toString(),
        criteriaHolder.getQueryCriteria());
    assertEquals(JRetentionPolicyEnum.IMPORTANT, criteriaHolder.castValue("important"));
    assertEquals(JRetentionPolicyEnum.REGULAR, criteriaHolder.castValue("REGULAR"));

    ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> criteriaHolder.castValue("UNKNOWN"));
    assertEquals(ErrorType.INCORRECT_FILTER_PARAMETERS, exception.getErrorType());
  }

  @Test
  void retentionPolicyEqualsAndInConditionsUseRetentionPolicyColumn() {
    String equalsSql = render(FilterCondition.builder()
        .eq(CRITERIA_RETENTION_POLICY, "important")
        .build());
    assertEquals(
        "\"launch\".\"retention_policy\" = cast('IMPORTANT' as \"public\".\"retention_policy_enum\")",
        equalsSql);

    String notEqualsSql = render(FilterCondition.builder()
        .withCondition(Condition.NOT_EQUALS)
        .withSearchCriteria(CRITERIA_RETENTION_POLICY)
        .withValue("REGULAR")
        .build());
    assertEquals(
        "\"launch\".\"retention_policy\" <> cast('REGULAR' as \"public\".\"retention_policy_enum\")",
        notEqualsSql);

    String inSql = render(FilterCondition.builder()
        .in(CRITERIA_RETENTION_POLICY, Lists.newArrayList("IMPORTANT", "regular"))
        .build());
    assertEquals(
        "\"launch\".\"retention_policy\" in ("
            + "cast('IMPORTANT' as \"public\".\"retention_policy_enum\"), "
            + "cast('REGULAR' as \"public\".\"retention_policy_enum\"))",
        inSql);
  }

  private static String render(FilterCondition filterCondition) {
    return DSL.using(SQLDialect.POSTGRES)
        .renderInlined(filterCondition.toCondition(FilterTarget.LAUNCH_TARGET)
            .get(ConditionType.WHERE));
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
