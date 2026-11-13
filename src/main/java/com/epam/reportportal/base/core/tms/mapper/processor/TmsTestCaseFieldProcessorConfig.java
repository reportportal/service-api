/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.tms.mapper.processor;

import com.epam.reportportal.base.core.events.domain.tms.TmsTestCaseHistoryOfActionsField;
import com.epam.reportportal.base.infrastructure.persistence.entity.activity.ActivityAction;
import com.epam.reportportal.base.model.activity.TestCaseActivityResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TmsTestCaseFieldProcessorConfig {

  @Bean
  public TmsTestCaseFieldProcessor nameProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.NAME,
        TestCaseActivityResource::getName,
        null,
        ActivityAction.UPDATE_TEST_CASE_NAME,
        null
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor descriptionProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.DESCRIPTION,
        TestCaseActivityResource::getDescription,
        ActivityAction.CREATE_TEST_CASE_DESCRIPTION,
        ActivityAction.UPDATE_TEST_CASE_DESCRIPTION,
        ActivityAction.DELETE_TEST_CASE_DESCRIPTION
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor priorityProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.PRIORITY,
        TestCaseActivityResource::getPriority,
        ActivityAction.CREATE_TEST_CASE_PRIORITY,
        ActivityAction.UPDATE_TEST_CASE_PRIORITY,
        ActivityAction.DELETE_TEST_CASE_PRIORITY
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor tagsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.TAGS,
        TestCaseActivityResource::getTags,
        ActivityAction.CREATE_TEST_CASE_TAGS,
        ActivityAction.UPDATE_TEST_CASE_TAGS,
        ActivityAction.DELETE_TEST_CASE_TAGS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor externalIdProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.EXTERNAL_ID,
        TestCaseActivityResource::getExternalId,
        ActivityAction.CREATE_TEST_CASE_EXTERNAL_ID,
        ActivityAction.UPDATE_TEST_CASE_EXTERNAL_ID,
        ActivityAction.DELETE_TEST_CASE_EXTERNAL_ID
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor testFolderIdProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.TEST_FOLDER_ID,
        TestCaseActivityResource::getTestFolderId,
        null,
        ActivityAction.UPDATE_TEST_CASE_TEST_FOLDER_ID,
        null
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor executionEstimationTimeProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.EXECUTION_ESTIMATION_TIME,
        TestCaseActivityResource::getExecutionEstimationTime,
        ActivityAction.CREATE_TEST_CASE_EXECUTION_ESTIMATION_TIME,
        ActivityAction.UPDATE_TEST_CASE_EXECUTION_ESTIMATION_TIME,
        ActivityAction.DELETE_TEST_CASE_EXECUTION_ESTIMATION_TIME
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor typeProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.TYPE,
        TestCaseActivityResource::getType,
        ActivityAction.CREATE_TEST_CASE_TYPE,
        ActivityAction.UPDATE_TEST_CASE_TYPE,
        ActivityAction.DELETE_TEST_CASE_TYPE
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor instructionsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.INSTRUCTIONS,
        TestCaseActivityResource::getInstructions,
        ActivityAction.CREATE_TEST_CASE_INSTRUCTIONS,
        ActivityAction.UPDATE_TEST_CASE_INSTRUCTIONS,
        ActivityAction.DELETE_TEST_CASE_INSTRUCTIONS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor expectedResultProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.EXPECTED_RESULT,
        TestCaseActivityResource::getExpectedResult,
        ActivityAction.CREATE_TEST_CASE_EXPECTED_RESULT,
        ActivityAction.UPDATE_TEST_CASE_EXPECTED_RESULT,
        ActivityAction.DELETE_TEST_CASE_EXPECTED_RESULT
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor preconditionsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.PRECONDITIONS,
        TestCaseActivityResource::getPreconditions,
        ActivityAction.CREATE_TEST_CASE_PRECONDITIONS,
        ActivityAction.UPDATE_TEST_CASE_PRECONDITIONS,
        ActivityAction.DELETE_TEST_CASE_PRECONDITIONS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor preconditionsAttachmentsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.PRECONDITIONS_ATTACHMENTS,
        TestCaseActivityResource::getPreconditionsAttachments,
        ActivityAction.CREATE_TEST_CASE_PRECONDITIONS_ATTACHMENTS,
        ActivityAction.UPDATE_TEST_CASE_PRECONDITIONS_ATTACHMENTS,
        ActivityAction.DELETE_TEST_CASE_PRECONDITIONS_ATTACHMENTS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor stepsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.STEPS,
        TestCaseActivityResource::getSteps,
        ActivityAction.CREATE_TEST_CASE_STEPS,
        ActivityAction.UPDATE_TEST_CASE_STEPS,
        ActivityAction.DELETE_TEST_CASE_STEPS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor requirementsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.REQUIREMENTS,
        TestCaseActivityResource::getRequirements,
        ActivityAction.CREATE_TEST_CASE_REQUIREMENTS,
        ActivityAction.UPDATE_TEST_CASE_REQUIREMENTS,
        ActivityAction.DELETE_TEST_CASE_REQUIREMENTS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor manualScenarioAttachmentsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.MANUAL_SCENARIO_ATTACHMENTS,
        TestCaseActivityResource::getManualScenarioAttachments,
        ActivityAction.CREATE_TEST_CASE_MANUAL_SCENARIO_ATTACHMENTS,
        ActivityAction.UPDATE_TEST_CASE_MANUAL_SCENARIO_ATTACHMENTS,
        ActivityAction.DELETE_TEST_CASE_MANUAL_SCENARIO_ATTACHMENTS
    );
  }

  @Bean
  public TmsTestCaseFieldProcessor manualScenarioStepAttachmentsProcessor() {
    return new TmsTmsTestCaseFieldProcessorImpl(
        TmsTestCaseHistoryOfActionsField.MANUAL_SCENARIO_STEP_ATTACHMENTS,
        TestCaseActivityResource::getManualScenarioStepAttachments,
        ActivityAction.CREATE_TEST_CASE_MANUAL_SCENARIO_STEP_ATTACHMENTS,
        ActivityAction.UPDATE_TEST_CASE_MANUAL_SCENARIO_STEP_ATTACHMENTS,
        ActivityAction.DELETE_TEST_CASE_MANUAL_SCENARIO_STEP_ATTACHMENTS
    );
  }
}
