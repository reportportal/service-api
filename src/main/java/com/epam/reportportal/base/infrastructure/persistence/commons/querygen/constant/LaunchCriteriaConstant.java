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

package com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant;

/**
 * Search criteria fields for Launch.
 *
 * @author Pavel Bortnik
 */
public final class LaunchCriteriaConstant {

  public static final String CRITERIA_LAUNCH_UUID = "uuid";
  public static final String CRITERIA_LAUNCH_MODE = "mode";
  public static final String CRITERIA_LAUNCH_STATUS = "status";
  public static final String CRITERIA_LAUNCH_NUMBER = "number";
  public static final String CRITERIA_LAUNCH_TYPE = "launchType";
  public static final String CRITERIA_TEST_PLAN_ID = "testPlanId";

  private LaunchCriteriaConstant() {
    //static only
  }

}
