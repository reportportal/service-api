package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_RETENTION_POLICY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_DISPLAY_ID;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void containsBindsEscapedPatternAsParameter() {
    org.jooq.Condition condition = FilterCondition.builder()
        .withCondition(Condition.CONTAINS)
        .withSearchCriteria("name")
        .withValue("100%_a'b\\c")
        .build()
        .toCondition(FilterTarget.LAUNCH_TARGET)
        .get(ConditionType.WHERE);

    org.jooq.Query query = DSL.using(SQLDialect.POSTGRES).selectOne().where(condition);
    assertEquals(java.util.List.of("%100\\%\\_a'b\\\\c%"), query.getBindValues());
    assertFalse(query.getSQL().contains("100"));
  }

  @Test
  void existsOnPlainColumnHonoursExplicitFalse() {
    assertEquals("\"tms_test_plan\".\"milestone_id\" is not null",
        renderTmsTestPlan(Condition.EXISTS, false, "true"));
    assertEquals("\"tms_test_plan\".\"milestone_id\" is null",
        renderTmsTestPlan(Condition.EXISTS, false, "false"));
    assertEquals("\"tms_test_plan\".\"milestone_id\" is null",
        renderTmsTestPlan(Condition.EXISTS, false, " FALSE "));
  }

  @Test
  void existsOnPlainColumnKeepsMeaningForNonBooleanValues() {
    // existing clients send ids/flags like "1", "10" or "0" with `ex` (e.g. filter.!ex.retryParentId=10)
    for (String value : new String[] {"1", "10", "0", "TRUE"}) {
      assertEquals("\"tms_test_plan\".\"milestone_id\" is not null",
          renderTmsTestPlan(Condition.EXISTS, false, value), value);
    }
    assertEquals("not (\"tms_test_plan\".\"milestone_id\" is not null)",
        renderTmsTestPlan(Condition.EXISTS, true, "10"));
  }

  @Test
  void tmsTestPlanMilestoneIdSupportsEqualsAndIn() {
    assertEquals("\"tms_test_plan\".\"milestone_id\" = 12",
        renderTmsTestPlan(Condition.EQUALS, false, "12"));
    assertEquals("\"tms_test_plan\".\"milestone_id\" in (1, 2)",
        renderTmsTestPlan(Condition.IN, false, "1,2"));
  }

  @Test
  void existsOnAggregateCriteriaIsUnchanged() {
    String sql = DSL.using(SQLDialect.POSTGRES)
        .renderInlined(FilterCondition.builder()
            .withCondition(Condition.EXISTS)
            .withSearchCriteria("ticketId")
            .withValue("false")
            .build()
            .toCondition(FilterTarget.TEST_ITEM_TARGET)
            .get(ConditionType.HAVING));
    assertTrue(sql.startsWith("coalesce(array_length(array_remove(array_agg("), sql);
    assertTrue(sql.endsWith(", null), 1), 0) = 0"), sql);

  }

  private static String renderTmsTestPlan(Condition condition, boolean negative, String value) {
    return DSL.using(SQLDialect.POSTGRES)
        .renderInlined(FilterCondition.builder()
            .withCondition(condition)
            .withNegative(negative)
            .withSearchCriteria("milestoneId")
            .withValue(value)
            .build()
            .toCondition(FilterTarget.TMS_TEST_PLAN_TARGET)
            .get(ConditionType.WHERE));
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
