/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.QueryBuilder.STATISTICS_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTIVITY_ORG_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTIVITY_ORG_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTIVITY_PROJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_EVENT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_OBJECT_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_PRIORITY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_SUBJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_SUBJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_SUBJECT_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_CREATION_DATE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_DESCRIPTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_END_TIME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAST_MODIFIED;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_OWNER;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_SLUG;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_START_TIME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_USER_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.IntegrationCriteriaConstant.CRITERIA_INTEGRATION_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.IssueCriteriaConstant.CRITERIA_ISSUE_AUTO_ANALYZED;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.IssueCriteriaConstant.CRITERIA_ISSUE_COMMENT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.IssueCriteriaConstant.CRITERIA_ISSUE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.IssueCriteriaConstant.CRITERIA_ISSUE_IGNORE_ANALYZER;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.IssueCriteriaConstant.CRITERIA_ISSUE_LOCATOR;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_COMPOSITE_SYSTEM_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_VALUE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.CRITERIA_LEVEL_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.KEY_VALUE_SEPARATOR;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ItemAttributeConstant.LAUNCH_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_NUMBER;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_STATUS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_UUID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_TEST_PLAN_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_ITEM_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_BINARY_CONTENT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_LEVEL;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_MESSAGE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_LOG_TIME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.LogCriteriaConstant.CRITERIA_TEST_ITEM_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_CREATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_LAST_LAUNCH_RUN;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_LAUNCHES;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_PROJECTS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_SLUG;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_UPDATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_USERS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_USER_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.OrganizationCriteriaConstant.CRITERIA_ORG_USER_ROLE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_ALLOCATED_STORAGE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_CREATION_DATE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ORGANIZATION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ORGANIZATION_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_CLUSTER_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_DURATION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_HAS_CHILDREN;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_HAS_RETRIES;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_HAS_STATS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_GROUP_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_ISSUE_TYPE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PARAMETER_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PARAMETER_VALUE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PARENT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATH;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_PATTERN_TEMPLATE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_RETRY_PARENT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_RETRY_PARENT_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_STATUS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_HASH;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TEST_CASE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_TICKET_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_UNIQUE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.CRITERIA_UUID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant.RETRY_PARENT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_ACCOUNT_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_ACTIVE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EMAIL;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EXPIRED;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EXTERNALID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_EXTERNAL_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULLNAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_FULL_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_INSTANCE_ROLE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_LAST_LOGIN;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_ROLE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_SYNCHRONIZATION_DATE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER_CREATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER_ORGANIZATION_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.UserCriteriaConstant.CRITERIA_USER_UPDATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsAttributeCriteriaConstant.CRITERIA_TMS_ATTRIBUTE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsAttributeCriteriaConstant.CRITERIA_TMS_ATTRIBUTE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsAttributeCriteriaConstant.CRITERIA_TMS_ATTRIBUTE_VALUE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_ATTRIBUTES;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_ATTRIBUTE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_ATTRIBUTE_VALUE;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_CREATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_DESCRIPTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXTERNAL_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_FOLDER_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_PLAN_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_PRIORITY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_SEARCH;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseCriteriaConstant.CRITERIA_TMS_TEST_CASE_UPDATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_PRIORITY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_STATUS;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_TEST_CASE_ATTRIBUTE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_TEST_CASE_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestCaseExecutionCriteriaConstant.CRITERIA_TMS_TEST_CASE_EXECUTION_TEST_ITEM_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_DESCRIPTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_PARENT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_ATTRIBUTES;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_ATTRIBUTE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_CASE_PRIORITY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_PLAN_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CHILD_ATTR;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CHILD_EXECUTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CHILD_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_LAUNCH_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_ATTRIBUTE_KEY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestFolderTestItemCriteriaConstant.CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_PRIORITY;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_CREATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_DESCRIPTION;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_NAME;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_SEARCH;
import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.tms.TmsTestPlanCriteriaConstant.CRITERIA_TMS_TEST_PLAN_UPDATED_AT;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.PROJECT_ID;
import static com.epam.reportportal.base.infrastructure.persistence.dao.constant.WidgetContentRepositoryConstants.USER_ID;
import static com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationFilter.PROJECTS_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.LAST_RUN;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.LAUNCHES_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo.USERS_QUANTITY;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ACTIVITY;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ATTACHMENT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.CLUSTERS_TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.DASHBOARD;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.DASHBOARD_WIDGET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.FILTER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.FILTER_CONDITION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.FILTER_SORT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.INTEGRATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.INTEGRATION_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_GROUP;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_TICKET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ISSUE_TYPE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ITEM_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LAUNCH;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.LOG;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.ORGANIZATION_USER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PARAMETER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PATTERN_TEMPLATE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PATTERN_TEMPLATE_TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.PROJECT_USER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.STATISTICS;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.STATISTICS_FIELD;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TEST_ITEM_RESULTS;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TICKET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_ATTRIBUTE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_CASE_EXECUTION;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_FOLDER;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_FOLDER_TEST_ITEM;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_PLAN;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.TMS_TEST_PLAN_TEST_CASE;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.USERS;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.Tables.WIDGET;
import static com.epam.reportportal.base.infrastructure.persistence.jooq.tables.JOwnedEntity.OWNED_ENTITY;
import static org.jooq.impl.DSL.choose;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;

import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.TestItemCriteriaConstant;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.query.JoinEntity;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.query.QuerySupplier;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.Activity;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import com.epam.reportportal.base.infrastructure.persistence.entity.filter.UserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationUserFilter;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectInfo;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectProfile;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolder;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestFolderTestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestPlan;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JIntegrationGroupEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JLaunchModeEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JLaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JOrganizationRoleEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JStatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.jooq.enums.JTestItemTypeEnum;
import com.google.common.collect.Lists;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;

public enum FilterTarget {

  PROJECT_TARGET(Project.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, PROJECT.ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ALLOCATED_STORAGE,
              PROJECT.ALLOCATED_STORAGE, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_NAME, PROJECT.NAME, String.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_KEY, PROJECT.KEY, String.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ORGANIZATION,
              PROJECT.ORGANIZATION, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_SLUG, PROJECT.SLUG, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ORGANIZATION_ID,
              PROJECT.ORGANIZATION_ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ATTRIBUTE_NAME,
              ATTRIBUTE.NAME,
              String.class,
              Lists.newArrayList(JoinEntity.of(PROJECT_ATTRIBUTE,
                  JoinType.LEFT_OUTER_JOIN,
                  PROJECT.ID.eq(PROJECT_ATTRIBUTE.PROJECT_ID)
              ), JoinEntity.of(ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                  PROJECT_ATTRIBUTE.ATTRIBUTE_ID.eq(ATTRIBUTE.ID)))
          ).get(),
          new CriteriaHolderBuilder().newBuilder(USERS_QUANTITY,
              USERS_QUANTITY,
              Long.class,
              Lists.newArrayList(JoinEntity.of(PROJECT_USER,
                  JoinType.LEFT_OUTER_JOIN,
                  PROJECT.ID.eq(PROJECT_USER.PROJECT_ID)
              ))
          ).withAggregateCriteria(DSL.countDistinct(PROJECT_USER.USER_ID).toString()).get(),
          new CriteriaHolderBuilder().newBuilder(LAUNCHES_QUANTITY,
                  LAUNCHES_QUANTITY,
                  Long.class,
                  Lists.newArrayList(
                      JoinEntity.of(LAUNCH, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(LAUNCH.PROJECT_ID)))
              )
              .withAggregateCriteria(
                  DSL.countDistinct(
                          choose().when(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS), LAUNCH.ID))
                      .toString())
              .get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(PROJECT.ID,
          PROJECT.NAME,
          PROJECT.KEY,
          PROJECT.SLUG,
          PROJECT.ORGANIZATION,
          PROJECT.ORGANIZATION_ID,
          PROJECT.CREATED_AT,
          PROJECT.METADATA,
          PROJECT_ATTRIBUTE.VALUE,
          ATTRIBUTE.NAME,
          PROJECT_USER.PROJECT_ID,
          PROJECT_USER.PROJECT_ROLE,
          PROJECT_USER.USER_ID,
          USERS.LOGIN
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(PROJECT);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(PROJECT_USER, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(PROJECT_USER.PROJECT_ID));
      query.addJoin(USERS, JoinType.LEFT_OUTER_JOIN, PROJECT_USER.USER_ID.eq(USERS.ID));
      query.addJoin(PROJECT_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
          PROJECT.ID.eq(PROJECT_ATTRIBUTE.PROJECT_ID));
      query.addJoin(ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
          PROJECT_ATTRIBUTE.ATTRIBUTE_ID.eq(ATTRIBUTE.ID));
      query.addJoin(LAUNCH, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(LAUNCH.PROJECT_ID));
    }

    @Override
    protected void joinTablesForFilter(QuerySupplier query) {

    }

    @Override
    protected Field<Long> idField() {
      return PROJECT.ID;
    }
  },

  PROJECT_INFO(ProjectInfo.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, PROJECT.ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_NAME, PROJECT.NAME, String.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_KEY, PROJECT.KEY, String.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ORGANIZATION,
              PROJECT.ORGANIZATION, String.class).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_CREATION_DATE,
              PROJECT.CREATED_AT, Timestamp.class).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_ID,
              PROJECT.ORGANIZATION_ID, Long.class).get(),

          new CriteriaHolderBuilder().newBuilder(USERS_QUANTITY, USERS_QUANTITY, Long.class)
              .withAggregateCriteria(DSL.countDistinct(PROJECT_USER.USER_ID).toString())
              .get(),

          new CriteriaHolderBuilder().newBuilder(LAST_RUN, LAST_RUN, Timestamp.class)
              .withAggregateCriteria(DSL.max(LAUNCH.START_TIME).toString())
              .get(),

          new CriteriaHolderBuilder().newBuilder(LAUNCHES_QUANTITY, LAUNCHES_QUANTITY, Long.class)
              .withAggregateCriteria(
                  DSL.countDistinct(
                          choose().when(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS), LAUNCH.ID))
                      .toString())
              .get()
      )
  ) {
    @Override
    public QuerySupplier getQuery() {
      SelectQuery<? extends Record> query = DSL.select(selectFields()).getQuery();
      addFrom(query);
      query.addGroupBy(PROJECT.ID, PROJECT.CREATED_AT, PROJECT.KEY);
      QuerySupplier querySupplier = new QuerySupplier(query);
      joinTables(querySupplier);
      return querySupplier;
    }

    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(DSL.countDistinct(PROJECT_USER.USER_ID).as(USERS_QUANTITY),
          DSL.countDistinct(choose().when(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS), LAUNCH.ID))
              .as(LAUNCHES_QUANTITY),
          DSL.max(LAUNCH.START_TIME).as(LAST_RUN),
          PROJECT.ID,
          PROJECT.CREATED_AT,
          PROJECT.KEY,
          PROJECT.SLUG,
          PROJECT.NAME,
          PROJECT.ORGANIZATION,
          PROJECT.ORGANIZATION_ID
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(PROJECT);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(PROJECT_USER, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(PROJECT_USER.PROJECT_ID));
      query.addJoin(LAUNCH, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(LAUNCH.PROJECT_ID));
    }

    @Override
    public QuerySupplier wrapQuery(SelectQuery<? extends Record> query) {
      throw new UnsupportedOperationException("Doesn't supported for Project Info query");
    }

    @Override
    public QuerySupplier wrapQuery(SelectQuery<? extends Record> query, String... excluding) {
      throw new UnsupportedOperationException("Doesn't supported for Project Info query");
    }

    @Override
    protected Field<Long> idField() {
      return PROJECT.ID;
    }
  },

  USER_TARGET(User.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, USERS.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_UUID, USERS.UUID, UUID.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_EXTERNAL_ID, USERS.EXTERNAL_ID, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_EXTERNALID, USERS.EXTERNAL_ID, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ACTIVE, USERS.ACTIVE, Boolean.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER, USERS.LOGIN, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_EMAIL, USERS.EMAIL, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_FULL_NAME, USERS.FULL_NAME, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_FULLNAME, USERS.FULL_NAME, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER_CREATED_AT, USERS.CREATED_AT, Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER_UPDATED_AT, USERS.UPDATED_AT, Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ROLE, USERS.ROLE, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_INSTANCE_ROLE, USERS.ROLE, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_TYPE, USERS.TYPE, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ACCOUNT_TYPE, USERS.TYPE, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_EXPIRED, USERS.EXPIRED, Boolean.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, PROJECT_USER.PROJECT_ID,
          Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT, PROJECT.NAME, List.class)
          .withAggregateCriteria(DSL.arrayAgg(PROJECT.NAME).toString())
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_KEY, PROJECT.KEY, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER_ORGANIZATION_ID, ORGANIZATION_USER.ORGANIZATION_ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAST_LOGIN,
          "(" + USERS.METADATA + "-> 'metadata' ->> 'last_login')::DOUBLE PRECISION ",
          Long.class
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_SYNCHRONIZATION_DATE,
          "(" + USERS.METADATA.getQualifiedName().toString()
              + "-> 'metadata' ->> 'synchronizationDate')::DOUBLE PRECISION ",
          Long.class
      ).get()

  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(USERS.ID,
          USERS.LOGIN,
          USERS.UUID,
          USERS.EXTERNAL_ID,
          USERS.ACTIVE,
          USERS.FULL_NAME,
          USERS.TYPE,
          USERS.CREATED_AT,
          USERS.UPDATED_AT,
          USERS.ATTACHMENT,
          USERS.ATTACHMENT_THUMBNAIL,
          USERS.EMAIL,
          USERS.EXPIRED,
          USERS.PASSWORD,
          USERS.ROLE,
          USERS.METADATA,
          PROJECT.NAME,
          PROJECT.KEY,
          PROJECT.SLUG,
          PROJECT_USER.PROJECT_ID,
          PROJECT_USER.PROJECT_ROLE,
          PROJECT_USER.USER_ID,
          ORGANIZATION.ID,
          ORGANIZATION.SLUG,
          ORGANIZATION.NAME,
          ORGANIZATION_USER.ORGANIZATION_ROLE
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(USERS);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(ORGANIZATION_USER, JoinType.LEFT_OUTER_JOIN, ORGANIZATION_USER.USER_ID.eq(USERS.ID));
      query.addJoin(ORGANIZATION, JoinType.LEFT_OUTER_JOIN, ORGANIZATION.ID.eq(ORGANIZATION_USER.ORGANIZATION_ID));

      query.addJoin(PROJECT_USER, JoinType.LEFT_OUTER_JOIN, ORGANIZATION_USER.USER_ID.eq(PROJECT_USER.USER_ID));
      query.addJoin(PROJECT, JoinType.LEFT_OUTER_JOIN, PROJECT_USER.PROJECT_ID.eq(PROJECT.ID));
    }

    @Override
    protected Field<Long> idField() {
      return USERS.ID;
    }
  },

  LAUNCH_TARGET(Launch.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, LAUNCH.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, LAUNCH.NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_DESCRIPTION, LAUNCH.DESCRIPTION, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_UUID, LAUNCH.UUID, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_START_TIME, LAUNCH.START_TIME,
          Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_END_TIME, LAUNCH.END_TIME, Timestamp.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, LAUNCH.PROJECT_ID, Long.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER_ID, LAUNCH.USER_ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_NUMBER, LAUNCH.NUMBER, Integer.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAST_MODIFIED, LAUNCH.LAST_MODIFIED,
          Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_MODE, LAUNCH.MODE,
          JLaunchModeEnum.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_STATUS, LAUNCH.STATUS,
          JStatusEnum.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_HAS_RETRIES, LAUNCH.HAS_RETRIES,
          Boolean.class).get(),

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ITEM_ATTRIBUTE_KEY,
          ITEM_ATTRIBUTE.KEY,
          String.class,
          Lists.newArrayList(JoinEntity.of(ITEM_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
              LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID)))
      ).withAggregateCriteria(
          DSL.arrayAggDistinct(ITEM_ATTRIBUTE.KEY).filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false))
              .toString()).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ITEM_ATTRIBUTE_VALUE,
              ITEM_ATTRIBUTE.VALUE,
              String.class,
              Lists.newArrayList(JoinEntity.of(ITEM_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                  LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID)))
          )
          .withAggregateCriteria(DSL.arrayAggDistinct(ITEM_ATTRIBUTE.VALUE)
              .filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false))
              .toString())
          .get(),
      new CriteriaHolderBuilder().newBuilder(
          CRITERIA_COMPOSITE_ATTRIBUTE,
          ITEM_ATTRIBUTE.KEY,
          String[].class,
          Lists.newArrayList(JoinEntity.of(LAUNCH_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
              LAUNCH.ID.eq(LAUNCH_ATTRIBUTE.LAUNCH_ID)))
      ).withAggregateCriteria(DSL.field("{0}::varchar[] || {1}::varchar[] || {2}::varchar[]",
          DSL.arrayAggDistinct(DSL.concat(LAUNCH_ATTRIBUTE.KEY, ":"))
              .filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false)),
          DSL.arrayAggDistinct(DSL.concat(LAUNCH_ATTRIBUTE.VALUE))
              .filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false)),
          DSL.arrayAgg(
                  DSL.concat(DSL.coalesce(LAUNCH_ATTRIBUTE.KEY, ""), DSL.val(KEY_VALUE_SEPARATOR),
                      LAUNCH_ATTRIBUTE.VALUE))
              .filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false))
      ).toString()).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER,
          USERS.LOGIN,
          String.class,
          Lists.newArrayList(
              JoinEntity.of(USERS, JoinType.LEFT_OUTER_JOIN, LAUNCH.USER_ID.eq(USERS.ID)))
      ).withAggregateCriteria(DSL.max(USERS.LOGIN).toString()).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_TYPE, LAUNCH.LAUNCH_TYPE,
          JLaunchTypeEnum.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_TEST_PLAN_ID, LAUNCH.TEST_PLAN_ID,
          Long.class).get()
  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      List<Field<?>> selectFields = new ArrayList<>();
      selectFields.addAll(getSelectSimpleFields());
      selectFields.addAll(getSelectAggregatedFields());

      return selectFields;
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(LAUNCH);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(ITEM_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
          LAUNCH.ID.eq(ITEM_ATTRIBUTE.LAUNCH_ID));
      query.addJoin(USERS, JoinType.LEFT_OUTER_JOIN, LAUNCH.USER_ID.eq(USERS.ID));
      query.addJoin(STATISTICS, JoinType.LEFT_OUTER_JOIN, LAUNCH.ID.eq(STATISTICS.LAUNCH_ID));
      query.addJoin(STATISTICS_FIELD, JoinType.LEFT_OUTER_JOIN,
          STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID));
    }

    @Override
    protected void joinTablesForFilter(QuerySupplier query) {
    }

    @Override
    protected Field<Long> idField() {
      return LAUNCH.ID;
    }

    @Override
    protected void addGroupBy(QuerySupplier query) {
      query.addGroupBy(getSelectSimpleFields());
    }

    @Override
    public boolean withGrouping() {
      return true;
    }

    private List<Field<?>> getSelectSimpleFields() {
      return Lists.newArrayList(LAUNCH.ID,
          LAUNCH.UUID,
          LAUNCH.NAME,
          LAUNCH.DESCRIPTION,
          LAUNCH.START_TIME,
          LAUNCH.END_TIME,
          LAUNCH.PROJECT_ID,
          LAUNCH.USER_ID,
          LAUNCH.NUMBER,
          LAUNCH.LAST_MODIFIED,
          LAUNCH.MODE,
          LAUNCH.STATUS,
          LAUNCH.HAS_RETRIES,
          LAUNCH.RERUN,
          LAUNCH.APPROXIMATE_DURATION,
          LAUNCH.RETENTION_POLICY,
          STATISTICS.S_COUNTER,
          STATISTICS_FIELD.NAME,
          USERS.ID,
          USERS.LOGIN
      );
    }

    private List<Field<?>> getSelectAggregatedFields() {
      return Lists.newArrayList(
          DSL.arrayAgg(DSL.jsonArray(
                  coalesce(ITEM_ATTRIBUTE.KEY, ""),
                  coalesce(ITEM_ATTRIBUTE.VALUE, ""),
                  ITEM_ATTRIBUTE.SYSTEM
              ))
              .as(ATTRIBUTE_ALIAS));
    }
  },

  TEST_ITEM_TARGET(TestItem.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, LAUNCH.PROJECT_ID, Long.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, TEST_ITEM.ITEM_ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, TEST_ITEM.NAME, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(TestItemCriteriaConstant.CRITERIA_TYPE,
                  TEST_ITEM.TYPE, JTestItemTypeEnum.class)
              .withAggregateCriteria(DSL.max(TEST_ITEM.TYPE).toString())
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_START_TIME, TEST_ITEM.START_TIME,
              Timestamp.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_DESCRIPTION, TEST_ITEM.DESCRIPTION,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_LAST_MODIFIED, TEST_ITEM.LAST_MODIFIED,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PATH, TEST_ITEM.PATH, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_UNIQUE_ID, TEST_ITEM.UNIQUE_ID,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_UUID, TEST_ITEM.UUID, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TEST_CASE_ID, TEST_ITEM.TEST_CASE_ID,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TEST_CASE_HASH, TEST_ITEM.TEST_CASE_HASH,
              Integer.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PARENT_ID, TEST_ITEM.PARENT_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_HAS_CHILDREN, TEST_ITEM.HAS_CHILDREN,
              Boolean.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_HAS_RETRIES, TEST_ITEM.HAS_RETRIES,
              Boolean.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_HAS_STATS, TEST_ITEM.HAS_STATS,
              Boolean.class).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_STATUS,
              TEST_ITEM_RESULTS.STATUS,
              JStatusEnum.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ))
          ).withAggregateCriteria(DSL.max(TEST_ITEM_RESULTS.STATUS).toString()).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_END_TIME,
              TEST_ITEM_RESULTS.END_TIME,
              Timestamp.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ))
          ).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_DURATION,
              TEST_ITEM_RESULTS.DURATION,
              Long.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ))
          ).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_PARAMETER_KEY,
              PARAMETER.KEY,
              String.class,
              Lists.newArrayList(JoinEntity.of(PARAMETER, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(PARAMETER.ITEM_ID)))
          ).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PARAMETER_VALUE,
              PARAMETER.VALUE,
              String.class,
              Lists.newArrayList(JoinEntity.of(PARAMETER, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(PARAMETER.ITEM_ID)))
          ).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_ID,
              ISSUE.ISSUE_ID,
              Long.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ), JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)))
          ).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_TYPE,
              ISSUE_TYPE.LOCATOR,
              String.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
                  ),
                  JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)),
                  JoinEntity.of(ISSUE_TYPE, JoinType.LEFT_OUTER_JOIN,
                      ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
              )
          ).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_TYPE_ID,
              ISSUE.ISSUE_TYPE,
              Long.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
                  ),
                  JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID))
              )
          ).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_AUTO_ANALYZED,
              ISSUE.AUTO_ANALYZED,
              Boolean.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ), JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)))
          ).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_IGNORE_ANALYZER,
              ISSUE.IGNORE_ANALYZER,
              Boolean.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ), JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)))
          ).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_COMMENT,
              ISSUE.ISSUE_DESCRIPTION,
              String.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
              ), JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)))
          ).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_LOCATOR,
              ISSUE_TYPE.LOCATOR,
              String.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
                  ),
                  JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)),
                  JoinEntity.of(ISSUE_TYPE, JoinType.LEFT_OUTER_JOIN,
                      ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
              )
          ).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_GROUP_ID,
              ISSUE_TYPE.ISSUE_GROUP_ID,
              Short.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
                  ),
                  JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)),
                  JoinEntity.of(ISSUE_TYPE, JoinType.LEFT_OUTER_JOIN,
                      ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID))
              )
          ).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_ID, TEST_ITEM.LAUNCH_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_LAUNCH_MODE, LAUNCH.MODE,
              JLaunchModeEnum.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PARENT_ID, TEST_ITEM.PARENT_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_RETRY_PARENT_ID, TEST_ITEM.RETRY_OF,
              Long.class).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ITEM_ATTRIBUTE_KEY,
                  ITEM_ATTRIBUTE.KEY,
                  List.class,
                  Lists.newArrayList(JoinEntity.of(ITEM_ATTRIBUTE,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
                  ))
              )
              .withAggregateCriteria(DSL.arrayAggDistinct(ITEM_ATTRIBUTE.KEY)
                  .filterWhere(DSL.coalesce(ITEM_ATTRIBUTE.SYSTEM, false).eq(false))
                  .toString())
              .get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_ITEM_ATTRIBUTE_VALUE,
                  ITEM_ATTRIBUTE.VALUE,
                  List.class,
                  Lists.newArrayList(JoinEntity.of(ITEM_ATTRIBUTE,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
                  ))
              )
              .withAggregateCriteria(DSL.arrayAggDistinct(ITEM_ATTRIBUTE.VALUE)
                  .filterWhere(DSL.coalesce(ITEM_ATTRIBUTE.SYSTEM, false).eq(false))
                  .toString())
              .get(),

          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_LEVEL_ATTRIBUTE,
                  ITEM_ATTRIBUTE.KEY,
                  List.class,
                  Lists.newArrayList(JoinEntity.of(ITEM_ATTRIBUTE,
                          JoinType.LEFT_OUTER_JOIN,
                          TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
                      ),
                      JoinEntity.of(LAUNCH_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                          LAUNCH.ID.eq(LAUNCH_ATTRIBUTE.LAUNCH_ID))
                  )
              )
              .withAggregateCriteria(DSL.field("array_cat({0}, {1})::varchar[]",
                  DSL.arrayAgg(DSL.concat(DSL.coalesce(ITEM_ATTRIBUTE.KEY, ""),
                      DSL.val(KEY_VALUE_SEPARATOR),
                      ITEM_ATTRIBUTE.VALUE
                  )).filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false)),
                  DSL.arrayAgg(DSL.concat(DSL.coalesce(LAUNCH_ATTRIBUTE.KEY, ""),
                      DSL.val(KEY_VALUE_SEPARATOR),
                      LAUNCH_ATTRIBUTE.VALUE
                  )).filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false))
              ).toString())
              .get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_COMPOSITE_ATTRIBUTE, ITEM_ATTRIBUTE.KEY,
              String[].class, Lists.newArrayList(
                  JoinEntity.of(ITEM_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)),
                  JoinEntity.of(LAUNCH_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                      LAUNCH.ID.eq(LAUNCH_ATTRIBUTE.LAUNCH_ID))
              )).withAggregateCriteria(DSL.field(
              "{0}::varchar[] || {1}::varchar[] || {2}::varchar[] || {3}::varchar[] || {4}::varchar[] || {5}::varchar[]",
              DSL.arrayAggDistinct(DSL.concat(LAUNCH_ATTRIBUTE.KEY, ":"))
                  .filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false)),
              DSL.arrayAggDistinct(DSL.concat(LAUNCH_ATTRIBUTE.VALUE))
                  .filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false)),
              DSL.arrayAgg(DSL.concat(DSL.coalesce(LAUNCH_ATTRIBUTE.KEY, ""),
                      DSL.val(KEY_VALUE_SEPARATOR),
                      LAUNCH_ATTRIBUTE.VALUE
                  ))
                  .filterWhere(LAUNCH_ATTRIBUTE.SYSTEM.eq(false)),
              DSL.arrayAggDistinct(DSL.concat(ITEM_ATTRIBUTE.KEY, ":"))
                  .filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false)),
              DSL.arrayAggDistinct(DSL.concat(ITEM_ATTRIBUTE.VALUE))
                  .filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false)),
              DSL.arrayAgg(DSL.concat(DSL.coalesce(ITEM_ATTRIBUTE.KEY, ""),
                      DSL.val(KEY_VALUE_SEPARATOR),
                      ITEM_ATTRIBUTE.VALUE
                  ))
                  .filterWhere(ITEM_ATTRIBUTE.SYSTEM.eq(false))
          ).toString()).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_COMPOSITE_SYSTEM_ATTRIBUTE,
              ITEM_ATTRIBUTE.KEY,
              String[].class, Lists.newArrayList(
                  JoinEntity.of(ITEM_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)),
                  JoinEntity.of(LAUNCH_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                      LAUNCH.ID.eq(LAUNCH_ATTRIBUTE.LAUNCH_ID))
              )).withAggregateCriteria(DSL.field(
              "{0}::varchar[] || {1}::varchar[] || {2}::varchar[] || {3}::varchar[] || {4}::varchar[] || {5}::varchar[]",
              DSL.arrayAggDistinct(DSL.concat(DSL.coalesce(LAUNCH_ATTRIBUTE.KEY, ""), ":"))
                  .filterWhere(DSL.coalesce(LAUNCH_ATTRIBUTE.SYSTEM, true).eq(true)),
              DSL.arrayAggDistinct(DSL.concat(DSL.coalesce(LAUNCH_ATTRIBUTE.VALUE, "")))
                  .filterWhere(DSL.coalesce(LAUNCH_ATTRIBUTE.SYSTEM, true).eq(true)),
              DSL.arrayAgg(DSL.concat(DSL.coalesce(LAUNCH_ATTRIBUTE.KEY, ""),
                      DSL.val(KEY_VALUE_SEPARATOR),
                      DSL.coalesce(LAUNCH_ATTRIBUTE.VALUE, "")
                  ))
                  .filterWhere(DSL.coalesce(LAUNCH_ATTRIBUTE.SYSTEM, true).eq(true)),
              DSL.arrayAggDistinct(DSL.concat((DSL.coalesce(ITEM_ATTRIBUTE.KEY, "")), ":")),
              DSL.arrayAggDistinct(DSL.concat(DSL.coalesce(ITEM_ATTRIBUTE.VALUE, ""))),
              DSL.arrayAgg(DSL.concat(DSL.coalesce(ITEM_ATTRIBUTE.KEY, ""),
                  DSL.val(KEY_VALUE_SEPARATOR),
                  DSL.coalesce(ITEM_ATTRIBUTE.VALUE, "")
              ))
          ).toString()).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_PATTERN_TEMPLATE_NAME,
              PATTERN_TEMPLATE.NAME,
              List.class,
              Lists.newArrayList(JoinEntity.of(PATTERN_TEMPLATE_TEST_ITEM,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID)
                  ),
                  JoinEntity.of(PATTERN_TEMPLATE,
                      JoinType.LEFT_OUTER_JOIN,
                      PATTERN_TEMPLATE_TEST_ITEM.PATTERN_ID.eq(PATTERN_TEMPLATE.ID)
                  )
              )
          ).withAggregateCriteria(DSL.arrayAggDistinct(PATTERN_TEMPLATE.NAME).toString()).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TICKET_ID,
              TICKET.TICKET_ID,
              String.class,
              Lists.newArrayList(JoinEntity.of(TEST_ITEM_RESULTS,
                      JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)
                  ),
                  JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                      TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID)),
                  JoinEntity.of(ISSUE_TICKET, JoinType.LEFT_OUTER_JOIN,
                      ISSUE.ISSUE_ID.eq(ISSUE_TICKET.ISSUE_ID)),
                  JoinEntity.of(TICKET, JoinType.LEFT_OUTER_JOIN,
                      ISSUE_TICKET.TICKET_ID.eq(TICKET.ID))
              )
          ).withAggregateCriteria(DSL.arrayAggDistinct(TICKET.TICKET_ID).toString()).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_CLUSTER_ID,
                  CLUSTERS_TEST_ITEM.CLUSTER_ID,
                  Long.class,
                  Lists.newArrayList(
                      JoinEntity.of(CLUSTERS_TEST_ITEM, JoinType.LEFT_OUTER_JOIN,
                          TEST_ITEM.ITEM_ID.eq(CLUSTERS_TEST_ITEM.ITEM_ID))
                  )
              ).withAggregateCriteria(DSL.arrayAggDistinct(CLUSTERS_TEST_ITEM.CLUSTER_ID).toString())
              .get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      List<Field<?>> selectFields = new ArrayList<>();
      selectFields.addAll(getSelectSimpleFields());
      selectFields.addAll(getSelectAggregatedFields());

      return selectFields;
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TEST_ITEM);
    }

    @Override
    protected Field<Long> idField() {
      return TEST_ITEM.ITEM_ID;
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(LAUNCH, JoinType.LEFT_OUTER_JOIN, TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID));
      query.addJoin(TEST_ITEM_RESULTS, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID));
      query.addJoin(ITEM_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID));
      query.addJoin(STATISTICS, JoinType.LEFT_OUTER_JOIN, TEST_ITEM.ITEM_ID.eq(STATISTICS.ITEM_ID));
      query.addJoin(STATISTICS_FIELD, JoinType.LEFT_OUTER_JOIN,
          STATISTICS.STATISTICS_FIELD_ID.eq(STATISTICS_FIELD.SF_ID));
      query.addJoin(PATTERN_TEMPLATE_TEST_ITEM, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM.ITEM_ID.eq(PATTERN_TEMPLATE_TEST_ITEM.ITEM_ID));
      query.addJoin(PATTERN_TEMPLATE, JoinType.LEFT_OUTER_JOIN,
          PATTERN_TEMPLATE_TEST_ITEM.PATTERN_ID.eq(PATTERN_TEMPLATE.ID));
      query.addJoin(ISSUE, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID));
      query.addJoin(ISSUE_TYPE, JoinType.LEFT_OUTER_JOIN, ISSUE.ISSUE_TYPE.eq(ISSUE_TYPE.ID));
      query.addJoin(ISSUE_GROUP, JoinType.LEFT_OUTER_JOIN,
          ISSUE_TYPE.ISSUE_GROUP_ID.eq(ISSUE_GROUP.ISSUE_GROUP_ID));
      query.addJoin(ISSUE_TICKET, JoinType.LEFT_OUTER_JOIN,
          ISSUE.ISSUE_ID.eq(ISSUE_TICKET.ISSUE_ID));
      query.addJoin(TICKET, JoinType.LEFT_OUTER_JOIN, ISSUE_TICKET.TICKET_ID.eq(TICKET.ID));
      query.addJoin(PARAMETER, JoinType.LEFT_OUTER_JOIN, TEST_ITEM.ITEM_ID.eq(PARAMETER.ITEM_ID));
      query.addJoin(LAUNCH_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
          LAUNCH.ID.eq(LAUNCH_ATTRIBUTE.LAUNCH_ID));
      query.addJoin(CLUSTERS_TEST_ITEM, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM.ITEM_ID.eq(CLUSTERS_TEST_ITEM.ITEM_ID));
    }

    @Override
    protected void joinTablesForFilter(QuerySupplier query) {
      query.addJoin(LAUNCH, JoinType.LEFT_OUTER_JOIN, TEST_ITEM.LAUNCH_ID.eq(LAUNCH.ID));
    }

    @Override
    protected void addGroupBy(QuerySupplier query) {
      query.addGroupBy(getSelectSimpleFields());
    }

    @Override
    public boolean withGrouping() {
      return true;
    }

    private List<Field<?>> getSelectSimpleFields() {
      return Lists.newArrayList(TEST_ITEM.ITEM_ID,
          TEST_ITEM.NAME,
          TEST_ITEM.CODE_REF,
          TEST_ITEM.TYPE,
          TEST_ITEM.START_TIME,
          TEST_ITEM.DESCRIPTION,
          TEST_ITEM.LAST_MODIFIED,
          TEST_ITEM.PATH,
          TEST_ITEM.UNIQUE_ID,
          TEST_ITEM.UUID,
          TEST_ITEM.TEST_CASE_ID,
          TEST_ITEM.TEST_CASE_HASH,
          TEST_ITEM.PARENT_ID,
          TEST_ITEM.RETRY_OF,
          TEST_ITEM.HAS_CHILDREN,
          TEST_ITEM.HAS_STATS,
          TEST_ITEM.HAS_RETRIES,
          TEST_ITEM.LAUNCH_ID,
          TEST_ITEM.ANALYSIS_OWNER_ID,
          TEST_ITEM_RESULTS.STATUS,
          TEST_ITEM_RESULTS.END_TIME,
          TEST_ITEM_RESULTS.DURATION,
          PARAMETER.ITEM_ID,
          PARAMETER.KEY,
          PARAMETER.VALUE,
          STATISTICS_FIELD.NAME,
          STATISTICS.S_COUNTER,
          ISSUE.ISSUE_ID,
          ISSUE.AUTO_ANALYZED,
          ISSUE.IGNORE_ANALYZER,
          ISSUE.ISSUE_DESCRIPTION,
          ISSUE_TYPE.ID,
          ISSUE_TYPE.LOCATOR,
          ISSUE_TYPE.ABBREVIATION,
          ISSUE_TYPE.HEX_COLOR,
          ISSUE_TYPE.ISSUE_NAME,
          ISSUE_GROUP.ISSUE_GROUP_,
          TICKET.ID,
          TICKET.BTS_PROJECT,
          TICKET.BTS_URL,
          TICKET.TICKET_ID,
          TICKET.URL,
          TICKET.PLUGIN_NAME,
          PATTERN_TEMPLATE.ID,
          PATTERN_TEMPLATE.NAME
      );
    }

    private List<Field<?>> getSelectAggregatedFields() {
      return Lists.newArrayList(
          DSL.arrayAgg(DSL.jsonArray(
                  coalesce(ITEM_ATTRIBUTE.KEY, ""),
                  coalesce(ITEM_ATTRIBUTE.VALUE, ""),
                  ITEM_ATTRIBUTE.SYSTEM
              ))
              .as(ATTRIBUTE_ALIAS));
    }
  },

  LOG_TARGET(Log.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, LOG.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_ID, LOG.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_TIME, LOG.LOG_TIME, Timestamp.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LAST_MODIFIED, LOG.LAST_MODIFIED,
          Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_LEVEL, LOG.LOG_LEVEL, LogLevel.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_MESSAGE, LOG.LOG_MESSAGE, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_TEST_ITEM_ID, LOG.ITEM_ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_LAUNCH_ID, LOG.LAUNCH_ID, Long.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_PROJECT_ID, LOG.PROJECT_ID, Long.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_LOG_BINARY_CONTENT,
          ATTACHMENT.FILE_ID,
          String.class,
          Lists.newArrayList(JoinEntity.of(ATTACHMENT, JoinType.LEFT_OUTER_JOIN,
              LOG.ATTACHMENT_ID.eq(ATTACHMENT.ID)))
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ITEM_LAUNCH_ID,
          TEST_ITEM.LAUNCH_ID,
          Long.class,
          Lists.newArrayList(
              JoinEntity.of(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)))
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_RETRY_PARENT_ID,
          TEST_ITEM.RETRY_OF,
          Long.class,
          Lists.newArrayList(
              JoinEntity.of(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)))
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_RETRY_PARENT_LAUNCH_ID,
          TEST_ITEM.as(RETRY_PARENT).LAUNCH_ID,
          Long.class,
          Lists.newArrayList(
              JoinEntity.of(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)),
              JoinEntity.of(TEST_ITEM.as(RETRY_PARENT),
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.RETRY_OF.eq(TEST_ITEM.as(RETRY_PARENT).ITEM_ID)
              )
          )
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PATH,
          TEST_ITEM.PATH,
          String.class,
          Lists.newArrayList(
              JoinEntity.of(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)))
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_STATUS,
          TEST_ITEM_RESULTS.STATUS,
          JStatusEnum.class,
          Lists.newArrayList(
              JoinEntity.of(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)),
              JoinEntity.of(TEST_ITEM_RESULTS, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID))
          )
      ).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ISSUE_AUTO_ANALYZED,
          ISSUE.AUTO_ANALYZED,
          Boolean.class,
          Lists.newArrayList(
              JoinEntity.of(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID)),
              JoinEntity.of(TEST_ITEM_RESULTS, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID)),
              JoinEntity.of(ISSUE, JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID))
          )
      ).get()
  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(LOG.ID,
          LOG.LOG_TIME,
          LOG.LOG_MESSAGE,
          LOG.LAST_MODIFIED,
          LOG.LOG_LEVEL,
          LOG.ITEM_ID,
          LOG.LAUNCH_ID,
          LOG.PROJECT_ID,
          LOG.ATTACHMENT_ID,
          ATTACHMENT.ID,
          ATTACHMENT.FILE_ID,
          ATTACHMENT.THUMBNAIL_ID,
          ATTACHMENT.CONTENT_TYPE,
          ATTACHMENT.FILE_SIZE,
          ATTACHMENT.PROJECT_ID,
          ATTACHMENT.LAUNCH_ID,
          ATTACHMENT.ITEM_ID,
          ATTACHMENT.FILE_NAME
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(LOG);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(TEST_ITEM, JoinType.LEFT_OUTER_JOIN, LOG.ITEM_ID.eq(TEST_ITEM.ITEM_ID));
      query.addJoin(TEST_ITEM_RESULTS, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID));
      query.addJoin(ISSUE, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM_RESULTS.RESULT_ID.eq(ISSUE.ISSUE_ID));
      query.addJoin(ATTACHMENT, JoinType.LEFT_OUTER_JOIN, LOG.ATTACHMENT_ID.eq(ATTACHMENT.ID));
    }

    @Override
    protected void joinTablesForFilter(QuerySupplier query) {

    }

    @Override
    protected Field<Long> idField() {
      return LOG.ID;
    }
  },

  ACTIVITY_TARGET(Activity.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, ACTIVITY.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ACTION, ACTIVITY.ACTION, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_CREATED_AT, ACTIVITY.CREATED_AT, Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_OBJECT_ID, ACTIVITY.OBJECT_ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_OBJECT_NAME, ACTIVITY.OBJECT_NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_OBJECT_TYPE, ACTIVITY.OBJECT_TYPE, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PRIORITY, ACTIVITY.PRIORITY, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, ACTIVITY.PROJECT_ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_SUBJECT_ID, ACTIVITY.SUBJECT_ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_SUBJECT_NAME, ACTIVITY.SUBJECT_NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_SUBJECT_TYPE, ACTIVITY.SUBJECT_TYPE, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ACTIVITY_PROJECT_NAME, PROJECT.NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_USER, USERS.LOGIN, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_EVENT_NAME, ACTIVITY.EVENT_NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ACTIVITY_ORG_ID, ORGANIZATION.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ACTIVITY_ORG_NAME, ORGANIZATION.NAME, String.class).get()

  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(ACTIVITY.ID,
          ACTIVITY.ACTION,
          ACTIVITY.EVENT_NAME,
          ACTIVITY.CREATED_AT,
          ACTIVITY.DETAILS,
          ACTIVITY.OBJECT_ID,
          ACTIVITY.OBJECT_NAME,
          ACTIVITY.OBJECT_TYPE,
          ACTIVITY.PRIORITY,
          ACTIVITY.PROJECT_ID,
          ACTIVITY.SUBJECT_ID,
          ACTIVITY.SUBJECT_NAME,
          ACTIVITY.SUBJECT_TYPE,
          USERS.LOGIN,
          USERS.ID.as(USER_ID),
          PROJECT.ID.as(PROJECT_ID),
          PROJECT.KEY,
          PROJECT.NAME,
          ORGANIZATION.ID.as(CRITERIA_ORG_ID),
          ORGANIZATION.NAME
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(ACTIVITY);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(USERS, JoinType.LEFT_OUTER_JOIN, ACTIVITY.SUBJECT_ID.eq(USERS.ID));
      query.addJoin(ORGANIZATION, JoinType.LEFT_OUTER_JOIN, ACTIVITY.ORGANIZATION_ID.eq(ORGANIZATION.ID));
      query.addJoin(PROJECT, JoinType.LEFT_OUTER_JOIN, ACTIVITY.PROJECT_ID.eq(PROJECT.ID));
    }

    @Override
    protected Field<Long> idField() {
      return ACTIVITY.ID;
    }

    @Override
    public QuerySupplier getQuery() {
      SelectQuery<? extends Record> query = DSL.select(selectFields()).getQuery();
      addFrom(query);
      query.addGroupBy(
          ACTIVITY.ID,
          ACTIVITY.ACTION,
          ACTIVITY.EVENT_NAME,
          ACTIVITY.CREATED_AT,
          ACTIVITY.DETAILS,
          ACTIVITY.OBJECT_ID,
          ACTIVITY.OBJECT_NAME,
          ACTIVITY.OBJECT_TYPE,
          ACTIVITY.PRIORITY,
          ACTIVITY.PROJECT_ID,
          ACTIVITY.SUBJECT_ID,
          ACTIVITY.SUBJECT_NAME,
          ACTIVITY.SUBJECT_TYPE,
          USERS.ID,
          USERS.LOGIN,
          PROJECT.ID,
          PROJECT.NAME,
          ORGANIZATION.ID,
          ORGANIZATION.NAME);
      QuerySupplier querySupplier = new QuerySupplier(query);
      joinTables(querySupplier);
      return querySupplier;
    }
  },

  INTEGRATION_TARGET(Integration.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, INTEGRATION.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, INTEGRATION.PROJECT_ID,
          String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_INTEGRATION_TYPE, INTEGRATION_TYPE.GROUP_TYPE,
              JIntegrationGroupEnum.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, INTEGRATION_TYPE.NAME, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_NAME, PROJECT.NAME, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_KEY, PROJECT.KEY, String.class)
          .get()
  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(INTEGRATION.ID,
          INTEGRATION.NAME,
          INTEGRATION.PROJECT_ID,
          INTEGRATION.TYPE,
          INTEGRATION.PARAMS,
          INTEGRATION.CREATOR,
          INTEGRATION.CREATION_DATE,
          INTEGRATION_TYPE.NAME,
          INTEGRATION_TYPE.GROUP_TYPE,
          PROJECT.NAME,
          PROJECT.KEY,
          PROJECT.SLUG,
          PROJECT.ORGANIZATION_ID
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(INTEGRATION);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(INTEGRATION_TYPE, JoinType.JOIN, INTEGRATION.TYPE.eq(INTEGRATION_TYPE.ID));
      query.addJoin(PROJECT, JoinType.JOIN, INTEGRATION.PROJECT_ID.eq(PROJECT.ID));
    }

    @Override
    protected Field<Long> idField() {
      return DSL.cast(INTEGRATION.ID, Long.class);
    }
  },

  DASHBOARD_TARGET(Dashboard.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, DASHBOARD.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, DASHBOARD.NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_CREATION_DATE, DASHBOARD.CREATION_DATE,
          Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, OWNED_ENTITY.PROJECT_ID,
              Long.class)
          .withAggregateCriteria(DSL.max(OWNED_ENTITY.PROJECT_ID).toString())
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_OWNER, OWNED_ENTITY.OWNER, String.class)
          .withAggregateCriteria(DSL.max(OWNED_ENTITY.OWNER).toString())
          .get()
  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(DASHBOARD.ID,
          DASHBOARD.NAME,
          DASHBOARD.DESCRIPTION,
          DASHBOARD.CREATION_DATE,
          DASHBOARD_WIDGET.WIDGET_OWNER,
          DASHBOARD_WIDGET.IS_CREATED_ON,
          DASHBOARD_WIDGET.WIDGET_ID,
          DASHBOARD_WIDGET.WIDGET_NAME,
          DASHBOARD_WIDGET.WIDGET_TYPE,
          DASHBOARD_WIDGET.WIDGET_HEIGHT,
          DASHBOARD_WIDGET.WIDGET_WIDTH,
          DASHBOARD_WIDGET.WIDGET_POSITION_X,
          DASHBOARD_WIDGET.WIDGET_POSITION_Y,
          WIDGET.WIDGET_OPTIONS,
          OWNED_ENTITY.PROJECT_ID,
          OWNED_ENTITY.OWNER
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(DASHBOARD);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(DASHBOARD_WIDGET, JoinType.LEFT_OUTER_JOIN,
          DASHBOARD.ID.eq(DASHBOARD_WIDGET.DASHBOARD_ID));
      query.addJoin(WIDGET, JoinType.LEFT_OUTER_JOIN, DASHBOARD_WIDGET.WIDGET_ID.eq(WIDGET.ID));
      query.addJoin(OWNED_ENTITY, JoinType.JOIN, DASHBOARD.ID.eq(OWNED_ENTITY.ID));
    }

    @Override
    protected Field<Long> idField() {
      return DASHBOARD.ID.cast(Long.class);
    }
  },

  WIDGET_TARGET(Widget.class, Arrays.asList(

      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, WIDGET.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, WIDGET.NAME, String.class)
          .withAggregateCriteria(DSL.max(WIDGET.NAME).toString())
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_DESCRIPTION, WIDGET.DESCRIPTION, String.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, OWNED_ENTITY.PROJECT_ID,
          Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_OWNER, OWNED_ENTITY.OWNER, String.class)
          .withAggregateCriteria(DSL.max(OWNED_ENTITY.OWNER).toString())
          .get()

  )) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(WIDGET.ID,
          WIDGET.NAME,
          WIDGET.WIDGET_TYPE,
          WIDGET.DESCRIPTION,
          WIDGET.ITEMS_COUNT,
          OWNED_ENTITY.PROJECT_ID,
          OWNED_ENTITY.OWNER
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(WIDGET);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(OWNED_ENTITY, JoinType.JOIN, WIDGET.ID.eq(OWNED_ENTITY.ID));
    }

    @Override
    protected Field<Long> idField() {
      return WIDGET.ID;
    }
  },

  USER_FILTER_TARGET(UserFilter.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, FILTER.ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, FILTER.NAME, String.class).get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, OWNED_ENTITY.PROJECT_ID,
                  Long.class)
              .withAggregateCriteria(DSL.max(OWNED_ENTITY.PROJECT_ID).toString())
              .get(),

          new CriteriaHolderBuilder().newBuilder(CRITERIA_OWNER, OWNED_ENTITY.OWNER, String.class)
              .withAggregateCriteria(DSL.max(OWNED_ENTITY.OWNER).toString())
              .get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(FILTER.ID,
          FILTER.NAME,
          FILTER.TARGET,
          FILTER.DESCRIPTION,
          FILTER_CONDITION.SEARCH_CRITERIA,
          FILTER_CONDITION.CONDITION,
          FILTER_CONDITION.VALUE,
          FILTER_CONDITION.NEGATIVE,
          FILTER_SORT.FIELD,
          FILTER_SORT.DIRECTION,
          OWNED_ENTITY.PROJECT_ID,
          OWNED_ENTITY.OWNER
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(FILTER);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(OWNED_ENTITY, JoinType.JOIN, FILTER.ID.eq(OWNED_ENTITY.ID));
      query.addJoin(FILTER_CONDITION, JoinType.LEFT_OUTER_JOIN,
          FILTER.ID.eq(FILTER_CONDITION.FILTER_ID));
      query.addJoin(FILTER_SORT, JoinType.LEFT_OUTER_JOIN, FILTER.ID.eq(FILTER_SORT.FILTER_ID));
    }

    @Override
    protected Field<Long> idField() {
      return FILTER.ID;
    }
  },

  ORGANIZATION_TARGET(OrganizationFilter.class, Arrays.asList(
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ID, ORGANIZATION.ID, Long.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_NAME, ORGANIZATION.NAME, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_SLUG, ORGANIZATION.SLUG, String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_CREATED_AT, ORGANIZATION.CREATED_AT,
          Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_UPDATED_AT, ORGANIZATION.UPDATED_AT,
          Timestamp.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_TYPE, ORGANIZATION.ORGANIZATION_TYPE,
          String.class).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_USERS, USERS_QUANTITY, Long.class)
          .withAggregateCriteria(DSL.countDistinct(ORGANIZATION_USER.USER_ID).toString())
          .get(),
      new CriteriaHolderBuilder()
          .newBuilder(CRITERIA_ORG_USER_ID, ORGANIZATION_USER.USER_ID, Long.class)
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_PROJECTS, PROJECTS_QUANTITY, Long.class)
          .withAggregateCriteria(DSL.countDistinct(PROJECT.ID).toString()).get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_LAST_LAUNCH_RUN, LAST_RUN, Timestamp.class)
          .withAggregateCriteria(DSL.max(LAUNCH.START_TIME).toString())
          .get(),
      new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_LAUNCHES, LAUNCHES_QUANTITY, Long.class)
          .withAggregateCriteria(DSL.countDistinct(LAUNCH.ID).toString())
          .get()

  )) {
    @Override
    public QuerySupplier getQuery() {
      SelectQuery<? extends Record> query = DSL.select(selectFields()).getQuery();
      addFrom(query);
      query.addGroupBy(ORGANIZATION.ID);
      QuerySupplier querySupplier = new QuerySupplier(query);
      joinTables(querySupplier);
      return querySupplier;
    }

    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(ORGANIZATION.ID,
          ORGANIZATION.NAME,
          ORGANIZATION.SLUG,
          ORGANIZATION.CREATED_AT,
          ORGANIZATION.UPDATED_AT,
          ORGANIZATION.EXTERNAL_ID,
          ORGANIZATION.ORGANIZATION_TYPE,
          ORGANIZATION.OWNER_ID,
          DSL.countDistinct(ORGANIZATION_USER.USER_ID).as(USERS_QUANTITY),
          DSL.countDistinct(PROJECT.ID).as(PROJECTS_QUANTITY),
          DSL.countDistinct(LAUNCH.ID).as(LAUNCHES_QUANTITY),
          DSL.max(LAUNCH.START_TIME).as(LAST_RUN)
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(ORGANIZATION);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(ORGANIZATION_USER,
          JoinType.LEFT_OUTER_JOIN,
          ORGANIZATION_USER.ORGANIZATION_ID.eq(ORGANIZATION.ID));

      query.addJoin(PROJECT,
          JoinType.LEFT_OUTER_JOIN,
          PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID));

      query.addJoin(LAUNCH,
          JoinType.LEFT_OUTER_JOIN,
          PROJECT.ID.eq(LAUNCH.PROJECT_ID).and(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS)));
    }

    @Override
    protected Field<Long> idField() {
      return ORGANIZATION.ID.cast(Long.class);
    }

  },

  PROJECT_PROFILE(ProjectProfile.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_ID, PROJECT.ORGANIZATION_ID,
                  Long.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_USER_ID, PROJECT_USER.USER_ID, Long.class)
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, PROJECT.ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_NAME, PROJECT.NAME, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_SLUG, PROJECT.SLUG, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_KEY, PROJECT.KEY, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_CREATED_AT, PROJECT.CREATED_AT,
              Timestamp.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_UPDATED_AT, PROJECT.UPDATED_AT,
              Timestamp.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_USERS, USERS_QUANTITY, Long.class)
              .withAggregateCriteria(DSL.countDistinct(PROJECT_USER.USER_ID).toString())
              .get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_LAST_LAUNCH_RUN, LAST_RUN,
                  Timestamp.class)
              .withAggregateCriteria(DSL.max(LAUNCH.START_TIME).toString())
              .get(),
          new CriteriaHolderBuilder()
              .newBuilder(CRITERIA_ORG_LAUNCHES, LAUNCHES_QUANTITY, Long.class)
              .withAggregateCriteria(
                  DSL.countDistinct(
                          choose()
                              .when(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS), LAUNCH.ID))
                      .toString())
              .get()
      )
  ) {
    @Override
    public QuerySupplier getQuery() {
      SelectQuery<? extends Record> query = DSL.select(selectFields()).getQuery();
      addFrom(query);
      query.addGroupBy(PROJECT.ID, PROJECT.CREATED_AT, PROJECT.KEY);
      QuerySupplier querySupplier = new QuerySupplier(query);
      joinTables(querySupplier);
      return querySupplier;
    }

    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(DSL.countDistinct(PROJECT_USER.USER_ID).as(USERS_QUANTITY),
          DSL.countDistinct(choose().when(LAUNCH.STATUS.ne(JStatusEnum.IN_PROGRESS), LAUNCH.ID
          )).as(LAUNCHES_QUANTITY),
          DSL.max(LAUNCH.START_TIME).as(LAST_RUN),
          PROJECT.ID,
          PROJECT.CREATED_AT,
          PROJECT.UPDATED_AT,
          PROJECT.KEY,
          PROJECT.SLUG,
          PROJECT.NAME,
          PROJECT.ORGANIZATION_ID
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(PROJECT);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(PROJECT_USER, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(PROJECT_USER.PROJECT_ID));
      query.addJoin(LAUNCH, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(LAUNCH.PROJECT_ID));
    }

    @Override
    public QuerySupplier wrapQuery(SelectQuery<? extends Record> query) {
      throw new UnsupportedOperationException("Operation not supported for ProjectProfile query");
    }

    @Override
    public QuerySupplier wrapQuery(SelectQuery<? extends Record> query, String... excluding) {
      throw new UnsupportedOperationException("Operation not supported for ProjectProfile query");
    }

    @Override
    protected Field<Long> idField() {
      return PROJECT.ID;
    }
  },

  ORGANIZATION_USERS(OrganizationUserFilter.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_ORG_ID, ORGANIZATION_USER.ORGANIZATION_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_FULL_NAME, USERS.FULL_NAME, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, PROJECT.ID, Long.class).get(),
          new CriteriaHolderBuilder()
              .newBuilder(CRITERIA_ORG_USER_ROLE, ORGANIZATION_USER.ORGANIZATION_ROLE, JOrganizationRoleEnum.class)
              .get()
      )
  ) {
    @Override
    public QuerySupplier getQuery() {
      SelectQuery<? extends Record> query = DSL.select(selectFields()).getQuery();
      addFrom(query);
      query.addGroupBy(
          ORGANIZATION_USER.USER_ID,
          USERS.METADATA,
          USERS.EMAIL,
          USERS.TYPE,
          USERS.ROLE,
          USERS.CREATED_AT,
          USERS.UPDATED_AT,
          USERS.FULL_NAME,
          USERS.EXTERNAL_ID,
          USERS.UUID,
          ORGANIZATION_USER.ORGANIZATION_ROLE);
      QuerySupplier querySupplier = new QuerySupplier(query);
      joinTables(querySupplier);
      return querySupplier;
    }

    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(DSL.countDistinct(PROJECT.ID).as(PROJECTS_QUANTITY),
          ORGANIZATION_USER.USER_ID,
          USERS.METADATA,
          USERS.EMAIL,
          USERS.TYPE,
          USERS.ROLE,
          USERS.CREATED_AT,
          USERS.UPDATED_AT,
          USERS.FULL_NAME,
          USERS.EXTERNAL_ID,
          USERS.UUID,
          ORGANIZATION_USER.ORGANIZATION_ROLE
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(ORGANIZATION_USER);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(USERS, JoinType.LEFT_OUTER_JOIN, USERS.ID.eq(ORGANIZATION_USER.USER_ID));
      query.addJoin(PROJECT_USER, JoinType.LEFT_OUTER_JOIN, PROJECT_USER.USER_ID.eq(ORGANIZATION_USER.USER_ID));
      query.addJoin(PROJECT, JoinType.LEFT_OUTER_JOIN, PROJECT.ID.eq(PROJECT_USER.PROJECT_ID)
          .and(PROJECT.ORGANIZATION_ID.eq(ORGANIZATION_USER.ORGANIZATION_ID)));
    }

    @Override
    public QuerySupplier wrapQuery(SelectQuery<? extends Record> query) {
      throw new UnsupportedOperationException("Operation not supported for OrganizationUserFilter query");
    }

    @Override
    public QuerySupplier wrapQuery(SelectQuery<? extends Record> query, String... excluding) {
      throw new UnsupportedOperationException("Operation not supported for OrganizationUserFilter query");
    }

    @Override
    protected Field<Long> idField() {
      return ORGANIZATION_USER.ORGANIZATION_ID;
    }
  },

  TMS_TEST_CASE_TARGET(TmsTestCase.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_ID, TMS_TEST_CASE.ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_NAME, TMS_TEST_CASE.NAME, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_DESCRIPTION, TMS_TEST_CASE.DESCRIPTION, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_PRIORITY, TMS_TEST_CASE.PRIORITY,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXTERNAL_ID, TMS_TEST_CASE.EXTERNAL_ID,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_CREATED_AT, TMS_TEST_CASE.CREATED_AT, Instant.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_UPDATED_AT, TMS_TEST_CASE.UPDATED_AT, Instant.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_FOLDER_ID, TMS_TEST_CASE.TEST_FOLDER_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_PROJECT_ID, TMS_TEST_FOLDER.PROJECT_ID,
              Long.class).get(),
          new CriteriaHolderBuilder()
              .newBuilder(
                  CRITERIA_TMS_TEST_CASE_PLAN_ID,
                  TMS_TEST_PLAN_TEST_CASE.TEST_PLAN_ID,
                  Long.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_TEST_PLAN_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE.ID.eq(TMS_TEST_PLAN_TEST_CASE.TEST_CASE_ID))
                  ))
              .get(),
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_CASE_SEARCH,
              TMS_TEST_CASE.SEARCH_VECTOR,
              String.class
          ).get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_CASE_ATTRIBUTES,
                  TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID,
                  Long.class
              )
              .withAggregateCriteria(
                  DSL.arrayAggDistinct(TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID).toString()
              )
              .get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_CASE_ATTRIBUTE_KEY,
                  TMS_ATTRIBUTE.KEY,
                  String.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID.eq(TMS_ATTRIBUTE.ID))
                  )
              )
              .withAggregateCriteria(DSL.arrayAggDistinct(TMS_ATTRIBUTE.KEY).toString())
              .get(),

          // --- NEW: Filter by Attribute Value ---
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_CASE_ATTRIBUTE_VALUE,
                  TMS_ATTRIBUTE.VALUE,
                  String.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID.eq(TMS_ATTRIBUTE.ID))
                  )
              )
              .withAggregateCriteria(DSL.arrayAggDistinct(TMS_ATTRIBUTE.VALUE).toString())
              .get()
      )
  ) {

    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(
          TMS_TEST_CASE.ID,
          TMS_TEST_CASE.NAME,
          TMS_TEST_CASE.DESCRIPTION,
          TMS_TEST_CASE.PRIORITY,
          TMS_TEST_CASE.EXTERNAL_ID,
          TMS_TEST_CASE.CREATED_AT,
          TMS_TEST_CASE.UPDATED_AT,
          TMS_TEST_CASE.TEST_FOLDER_ID
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TMS_TEST_CASE);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(TMS_TEST_FOLDER, JoinType.JOIN,
          TMS_TEST_FOLDER.ID.eq(TMS_TEST_CASE.TEST_FOLDER_ID));
      query.addJoin(TMS_TEST_CASE_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
          TMS_TEST_CASE_ATTRIBUTE.TEST_CASE_ID.eq(TMS_TEST_CASE.ID));
    }

    @Override
    protected Field<Long> idField() {
      return TMS_TEST_CASE.ID;
    }
  },

  TMS_TEST_PLAN_TARGET(TmsTestPlan.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_PLAN_ID, TMS_TEST_PLAN.ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_PLAN_NAME, TMS_TEST_PLAN.NAME,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_PLAN_DESCRIPTION, TMS_TEST_PLAN.DESCRIPTION,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_PLAN_CREATED_AT, TMS_TEST_CASE.CREATED_AT, Instant.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_PLAN_UPDATED_AT, TMS_TEST_CASE.UPDATED_AT, Instant.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_PLAN_PROJECT_ID, TMS_TEST_PLAN.PROJECT_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_PLAN_SEARCH,
              TMS_TEST_PLAN.SEARCH_VECTOR,
              String.class
          ).get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(
          TMS_TEST_PLAN.ID,
          TMS_TEST_PLAN.NAME,
          TMS_TEST_PLAN.DESCRIPTION,
          TMS_TEST_PLAN.PROJECT_ID
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TMS_TEST_PLAN);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      // No joins needed for basic fields
    }

    @Override
    protected Field<Long> idField() {
      return TMS_TEST_PLAN.ID;
    }
  },

  TMS_TEST_FOLDER_TARGET(TmsTestFolder.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_FOLDER_ID, TMS_TEST_FOLDER.ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_FOLDER_NAME, TMS_TEST_FOLDER.NAME,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_FOLDER_DESCRIPTION, TMS_TEST_FOLDER.DESCRIPTION,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_FOLDER_PARENT_ID, TMS_TEST_FOLDER.PARENT_ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_FOLDER_PROJECT_ID, TMS_TEST_FOLDER.PROJECT_ID,
              Long.class).get(),
          new CriteriaHolderBuilder()
              .newBuilder(
                  CRITERIA_TMS_TEST_FOLDER_TEST_PLAN_ID,
                  TMS_TEST_PLAN_TEST_CASE.TEST_PLAN_ID,
                  Long.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_FOLDER.ID.eq(TMS_TEST_CASE.TEST_FOLDER_ID)),
                      JoinEntity.of(TMS_TEST_PLAN_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE.ID.eq(TMS_TEST_PLAN_TEST_CASE.TEST_CASE_ID))
                  ))
              .get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_FOLDER_TEST_CASE_NAME,
                  TMS_TEST_CASE.NAME,
                  String.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_FOLDER.ID.eq(TMS_TEST_CASE.TEST_FOLDER_ID))
                  ))
              .get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_FOLDER_TEST_CASE_PRIORITY,
                  TMS_TEST_CASE.PRIORITY,
                  String.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_FOLDER.ID.eq(TMS_TEST_CASE.TEST_FOLDER_ID))
                  ))
              .get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_FOLDER_TEST_CASE_ATTRIBUTES,
                  TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID,
                  Long.class,
                  Lists.newArrayList(
                      JoinEntity.of(TMS_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_FOLDER.ID.eq(TMS_TEST_CASE.TEST_FOLDER_ID)),
                      JoinEntity.of(TMS_TEST_CASE_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE_ATTRIBUTE.TEST_CASE_ID.eq(TMS_TEST_CASE.ID))
                  ))
              .withAggregateCriteria(
                  DSL.arrayAggDistinct(TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID).toString()
              )
              .get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_FOLDER_TEST_CASE_ATTRIBUTE_KEY,
                  TMS_ATTRIBUTE.KEY,
                  String.class,
                  Lists.newArrayList(
                      // Join 1: Folder -> Test Case
                      JoinEntity.of(TMS_TEST_CASE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_FOLDER.ID.eq(TMS_TEST_CASE.TEST_FOLDER_ID)),
                      // Join 2: Test Case -> Attribute Link
                      JoinEntity.of(TMS_TEST_CASE_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE_ATTRIBUTE.TEST_CASE_ID.eq(TMS_TEST_CASE.ID)),
                      // Join 3: Attribute Link -> Attribute Definition (to get Key)
                      JoinEntity.of(TMS_ATTRIBUTE, JoinType.LEFT_OUTER_JOIN,
                          TMS_TEST_CASE_ATTRIBUTE.ATTRIBUTE_ID.eq(TMS_ATTRIBUTE.ID))
                  ))
              .withAggregateCriteria(
                  DSL.arrayAggDistinct(TMS_ATTRIBUTE.KEY).toString()
              )
              .get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(
          TMS_TEST_FOLDER.ID,
          TMS_TEST_FOLDER.NAME,
          TMS_TEST_FOLDER.DESCRIPTION,
          TMS_TEST_FOLDER.PARENT_ID,
          TMS_TEST_FOLDER.PROJECT_ID,
          TMS_TEST_FOLDER.INDEX
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TMS_TEST_FOLDER);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
    }

    @Override
    protected Field<Long> idField() {
      return TMS_TEST_FOLDER.ID;
    }
  },

  TMS_ATTRIBUTE_TARGET(TmsAttribute.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_ATTRIBUTE_ID, TMS_ATTRIBUTE.ID,
              Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_ATTRIBUTE_KEY, TMS_ATTRIBUTE.KEY,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_ATTRIBUTE_VALUE, TMS_ATTRIBUTE.VALUE,
              String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_PROJECT_ID, TMS_ATTRIBUTE.PROJECT_ID,
              Long.class).get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(
          TMS_ATTRIBUTE.ID,
          TMS_ATTRIBUTE.KEY,
          TMS_ATTRIBUTE.VALUE,
          TMS_ATTRIBUTE.PROJECT_ID
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TMS_ATTRIBUTE);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      // No joins needed for basic fields
    }

    @Override
    protected Field<Long> idField() {
      return TMS_ATTRIBUTE.ID;
    }
  },

  TMS_TEST_FOLDER_TEST_ITEM_TARGET(TmsTestFolderTestItem.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_ID,
              TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID, Long.class
          ).get(),
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_LAUNCH_ID,
              TMS_TEST_FOLDER_TEST_ITEM.LAUNCH_ID, Long.class
          ).get(),
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_NAME,
              CHILD_EXECUTION.NAME, String.class,
              Lists.newArrayList(
                  JoinEntity.of(CHILD_ITEM, JoinType.LEFT_OUTER_JOIN,
                      CHILD_ITEM.PARENT_ID.eq(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID)),
                  JoinEntity.of(CHILD_EXECUTION, JoinType.LEFT_OUTER_JOIN,
                      CHILD_EXECUTION.TEST_ITEM_ID.eq(CHILD_ITEM.ITEM_ID))
              )
          ).get(),
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_PRIORITY,
              CHILD_EXECUTION.PRIORITY, String.class,
              Lists.newArrayList(
                  JoinEntity.of(CHILD_ITEM, JoinType.LEFT_OUTER_JOIN,
                      CHILD_ITEM.PARENT_ID.eq(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID)),
                  JoinEntity.of(CHILD_EXECUTION, JoinType.LEFT_OUTER_JOIN,
                      CHILD_EXECUTION.TEST_ITEM_ID.eq(CHILD_ITEM.ITEM_ID))
              )
          ).get(),
          new CriteriaHolderBuilder().newBuilder(
                  CRITERIA_TMS_TEST_FOLDER_TEST_ITEM_TEST_CASE_ATTRIBUTE_KEY,
                  CHILD_ATTR.VALUE, // Filter by VALUE because key is mapped to value
                  List.class,
                  Lists.newArrayList(
                      JoinEntity.of(CHILD_ITEM, JoinType.LEFT_OUTER_JOIN,
                          CHILD_ITEM.PARENT_ID.eq(TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID)),
                      JoinEntity.of(CHILD_ATTR, JoinType.LEFT_OUTER_JOIN,
                          CHILD_ATTR.ITEM_ID.eq(CHILD_ITEM.ITEM_ID)
                              .and(CHILD_ATTR.KEY.eq("tag")) // Hardcoded key check
                              .and(DSL.coalesce(CHILD_ATTR.SYSTEM, false).eq(false)))
                  )
              )
              .withAggregateCriteria(
                  DSL.arrayAggDistinct(CHILD_ATTR.VALUE) // Aggregate VALUE
                      .filterWhere(CHILD_ATTR.KEY.eq("tag") // Filter agg by key
                          .and(DSL.coalesce(CHILD_ATTR.SYSTEM, false).eq(false)))
                      .toString()
              )
              .get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(
          TMS_TEST_FOLDER_TEST_ITEM.ID,
          TMS_TEST_FOLDER_TEST_ITEM.LAUNCH_ID,
          TMS_TEST_FOLDER_TEST_ITEM.TEST_FOLDER_ID,
          TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID,
          TMS_TEST_FOLDER_TEST_ITEM.NAME,
          TMS_TEST_FOLDER_TEST_ITEM.DESCRIPTION
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TMS_TEST_FOLDER_TEST_ITEM);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(TEST_ITEM, JoinType.JOIN,
          TMS_TEST_FOLDER_TEST_ITEM.TEST_ITEM_ID.eq(TEST_ITEM.ITEM_ID));
    }

    @Override
    protected void joinTablesForFilter(QuerySupplier query) {
      // Empty - joins are added dynamically from CriteriaHolder join chains
    }

    @Override
    protected Field<Long> idField() {
      return TMS_TEST_FOLDER_TEST_ITEM.ID;
    }
  },

  TMS_TEST_CASE_EXECUTION_TARGET(TmsTestCaseExecution.class,
      Arrays.asList(
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_ID,
              TMS_TEST_CASE_EXECUTION.ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_TEST_ITEM_ID,
              TMS_TEST_CASE_EXECUTION.TEST_ITEM_ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_TEST_CASE_ID,
              TMS_TEST_CASE_EXECUTION.TEST_CASE_ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_LAUNCH_ID,
              TMS_TEST_CASE_EXECUTION.LAUNCH_ID, Long.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_PRIORITY,
              TMS_TEST_CASE_EXECUTION.PRIORITY, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_NAME,
              TMS_TEST_CASE_EXECUTION.NAME, String.class).get(),
          new CriteriaHolderBuilder().newBuilder(CRITERIA_TMS_TEST_CASE_EXECUTION_STATUS,
              TEST_ITEM_RESULTS.STATUS,
              JStatusEnum.class
          ).get(),
          new CriteriaHolderBuilder().newBuilder(
              CRITERIA_TMS_TEST_CASE_EXECUTION_TEST_CASE_ATTRIBUTE_KEY,
              ITEM_ATTRIBUTE.VALUE, // Filter by VALUE
              List.class,
              Lists.newArrayList(JoinEntity.of(ITEM_ATTRIBUTE,
                  JoinType.LEFT_OUTER_JOIN,
                  TEST_ITEM.ITEM_ID.eq(ITEM_ATTRIBUTE.ITEM_ID)
                      .and(ITEM_ATTRIBUTE.KEY.eq("tag")) // Hardcoded key check
                      .and(DSL.coalesce(ITEM_ATTRIBUTE.SYSTEM, false).eq(false))
              ))
          ).get()
      )
  ) {
    @Override
    protected Collection<? extends SelectField> selectFields() {
      return Lists.newArrayList(
          TMS_TEST_CASE_EXECUTION.ID,
          TMS_TEST_CASE_EXECUTION.TEST_ITEM_ID,
          TMS_TEST_CASE_EXECUTION.TEST_CASE_ID,
          TMS_TEST_CASE_EXECUTION.LAUNCH_ID,
          TMS_TEST_CASE_EXECUTION.TEST_CASE_VERSION_ID,
          TMS_TEST_CASE_EXECUTION.PRIORITY,
          TMS_TEST_CASE_EXECUTION.TEST_CASE_SNAPSHOT,
          TEST_ITEM.START_TIME,
          TEST_ITEM.NAME,
          TEST_ITEM.PARENT_ID,
          TEST_ITEM_RESULTS.RESULT_ID,
          TEST_ITEM_RESULTS.STATUS,
          TEST_ITEM_RESULTS.END_TIME
      );
    }

    @Override
    protected void addFrom(SelectQuery<? extends Record> query) {
      query.addFrom(TMS_TEST_CASE_EXECUTION);
    }

    @Override
    protected void joinTables(QuerySupplier query) {
      query.addJoin(TEST_ITEM, JoinType.LEFT_OUTER_JOIN,
          TMS_TEST_CASE_EXECUTION.TEST_ITEM_ID.eq(TEST_ITEM.ITEM_ID));
      query.addJoin(TEST_ITEM_RESULTS, JoinType.LEFT_OUTER_JOIN,
          TEST_ITEM.ITEM_ID.eq(TEST_ITEM_RESULTS.RESULT_ID));
    }

    @Override
    protected Field<Long> idField() {
      return TMS_TEST_CASE_EXECUTION.ID;
    }
  };

  public static final String FILTERED_QUERY = "filtered";
  public static final String ATTRIBUTE_ALIAS = "attribute";
  public static final String FILTERED_ID = "id";

  private final Class<?> clazz;
  private final List<CriteriaHolder> criteriaHolders;

  FilterTarget(Class<?> clazz, List<CriteriaHolder> criteriaHolders) {
    this.clazz = clazz;
    this.criteriaHolders = criteriaHolders;
  }

  public static FilterTarget findByClass(Class<?> clazz) {
    return Arrays.stream(values())
        .filter(val -> val.clazz.equals(clazz))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("No target query builder for clazz %s", clazz)));
  }

  public QuerySupplier getQuery() {
    SelectQuery<? extends Record> query = DSL.select(idField().as(FILTERED_ID)).getQuery();
    addFrom(query);
    query.addGroupBy(idField());

    QuerySupplier querySupplier = new QuerySupplier(query);
    joinTablesForFilter(querySupplier);
    return querySupplier;
  }

  protected abstract Collection<? extends SelectField> selectFields();

  protected abstract void addFrom(SelectQuery<? extends Record> query);

  protected abstract void joinTables(QuerySupplier query);

  protected void joinTablesForFilter(QuerySupplier query) {
    joinTables(query);
  }

  protected void addGroupBy(QuerySupplier query) {
  }

  protected abstract Field<Long> idField();

  public QuerySupplier wrapQuery(SelectQuery<? extends Record> query) {
    SelectQuery<Record> wrappedQuery = DSL.with(FILTERED_QUERY).as(query).select(selectFields())
        .getQuery();
    addFrom(wrappedQuery);
    QuerySupplier querySupplier = new QuerySupplier(wrappedQuery);
    querySupplier.addJoin(DSL.table(DSL.name(FILTERED_QUERY)),
        JoinType.JOIN,
        idField().eq(field(DSL.name(FILTERED_QUERY, FILTERED_ID), Long.class))
    );
    joinTables(querySupplier);
    addGroupBy(querySupplier);
    return querySupplier;
  }

  public QuerySupplier wrapQuery(SelectQuery<? extends Record> query, String... excluding) {
    List<String> excludingFields = Lists.newArrayList(excluding);
    List<? extends SelectField> fields = selectFields().stream()
        .filter(it -> !excludingFields.contains(it.getName()))
        .collect(Collectors.toList());
    SelectQuery<Record> wrappedQuery = DSL.with(FILTERED_QUERY).as(query).select(fields).getQuery();
    addFrom(wrappedQuery);
    QuerySupplier querySupplier = new QuerySupplier(wrappedQuery);
    querySupplier.addJoin(DSL.table(DSL.name(FILTERED_QUERY)),
        JoinType.JOIN,
        idField().eq(field(DSL.name(FILTERED_QUERY, FILTERED_ID), Long.class))
    );
    joinTables(querySupplier);
    addGroupBy(querySupplier);
    return querySupplier;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public List<CriteriaHolder> getCriteriaHolders() {
    return criteriaHolders;
  }

  public Optional<CriteriaHolder> getCriteriaByFilter(String filterCriteria) {
   /*
    creates criteria holder for statistics search criteria cause there
    can be custom statistics so we can't know it till this moment
   */
    if (filterCriteria != null && filterCriteria.startsWith(STATISTICS_KEY)) {
      return Optional.of(new CriteriaHolderBuilder().newBuilder(filterCriteria,
          DSL.coalesce(
              DSL.max(STATISTICS.S_COUNTER).filterWhere(STATISTICS_FIELD.NAME.eq(filterCriteria)),
              0).toString(),
          Long.class
      ).get());
    }
    return criteriaHolders.stream()
        .filter(holder -> holder.getFilterCriteria().equals(filterCriteria)).findAny();
  }

  public boolean withGrouping() {
    return false;
  }
}
