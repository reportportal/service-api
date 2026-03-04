package com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms;

import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ITEM_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_EXECUTION;

import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JItemAttribute;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JTestItem;
import com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JTmsTestCaseExecution;

/**
 * Criteria constants for TMS Test Folder Test Item filtering.
 * Used to filter launch folders (SUITE items) by their child test case execution properties.
 */
public final class TmsTestFolderTestItemCriteriaConstant {

  public static final String CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_ID = "id";
  public static final String CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_LAUNCH_ID = "launchId";
  public static final String CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_NAME = "testCaseName";
  public static final String CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_PRIORITY = "testCasePriority";
  public static final String CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_ATTRIBUTE_KEY = "testCaseAttributeKey";

  /**
   * Alias for child test_item table (children of folder SUITE).
   */
  public static final JTestItem CHILD_ITEM = TEST_ITEM.as("child_item");

  /**
   * Alias for child tms_test_case_execution table (execution linked to child test item).
   */
  public static final JTmsTestCaseExecution CHILD_EXECUTION = TMS_TEST_CASE_EXECUTION.as("child_execution");

  /**
   * Alias for child item_attribute table (attributes of child test item).
   */
  public static final JItemAttribute CHILD_ATTR = ITEM_ATTRIBUTE.as("child_attr");

  private TmsTestFolderTestItemCriteriaConstant() {
    // static only
  }
}
