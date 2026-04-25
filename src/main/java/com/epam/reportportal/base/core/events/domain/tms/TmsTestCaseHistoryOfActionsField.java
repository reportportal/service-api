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

package com.epam.reportportal.base.core.events.domain.tms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TmsTestCaseHistoryOfActionsField {
    NAME("name"),
    DESCRIPTION("description"),
    PRIORITY("priority"),
    EXTERNAL_ID("externalId"),
    TEST_FOLDER_ID("testFolderId"),
    TAGS("tags"),
    EXECUTION_ESTIMATION_TIME("executionEstimationTime"),
    TYPE("type"),
    INSTRUCTIONS("instructions"),
    EXPECTED_RESULT("expectedResult"),
    PRECONDITIONS("preconditions"),
    PRECONDITIONS_ATTACHMENTS("preconditionsAttachments"),
    STEPS("steps"),
    REQUIREMENTS("requirements"),
    MANUAL_SCENARIO_ATTACHMENTS("manualScenarioAttachments"),
    MANUAL_SCENARIO_STEP_ATTACHMENTS("manualScenarioStepAttachments");

    private final String value;
}
