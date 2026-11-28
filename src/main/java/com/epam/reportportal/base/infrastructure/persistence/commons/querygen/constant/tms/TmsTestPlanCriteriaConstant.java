package com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms;

public final class TmsTestPlanCriteriaConstant {

  public static final String CRITERIA_TMS_TEST_PLAN_ID = "id";
  public static final String CRITERIA_TMS_TEST_PLAN_NAME = "name";
  public static final String CRITERIA_TMS_TEST_PLAN_DESCRIPTION = "description";
  public static final String CRITERIA_TMS_TEST_PLAN_PROJECT_ID = "projectId";
  public static final String CRITERIA_TMS_TEST_PLAN_SEARCH = "search";
  /**
   * Matches the test plan name or the name of its milestone ("Plan name, Milestone name").
   */
  public static final String CRITERIA_TMS_TEST_PLAN_NAME_OR_MILESTONE_NAME = "nameOrMilestoneName";
  public static final String CRITERIA_TMS_TEST_PLAN_CREATED_AT = "createdAt";
  public static final String CRITERIA_TMS_TEST_PLAN_UPDATED_AT = "updatedAt";

  private TmsTestPlanCriteriaConstant() {
    //static only
  }
}
