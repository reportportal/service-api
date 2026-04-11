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
 * Activity search criteria fields.
 *
 * @author Ivan Budaev
 */
public final class ActivityCriteriaConstant {

  public static final String CRITERIA_ACTION = "action";
  public static final String CRITERIA_SUBJECT_TYPE = "subjectType";
  public static final String CRITERIA_OBJECT_ID = "objectId";
  public static final String CRITERIA_OBJECT_TYPE = "objectType";
  public static final String CRITERIA_PRIORITY = "priority";
  public static final String CRITERIA_CREATED_AT = "createdAt";
  public static final String CRITERIA_OBJECT_NAME = "objectName";
  public static final String CRITERIA_EVENT_NAME = "eventName";
  public static final String CRITERIA_SUBJECT_ID = "subjectId";
  public static final String CRITERIA_SUBJECT_NAME = "subjectName";
  public static final String CRITERIA_ACTIVITY_PROJECT_NAME = "projectName";
  public static final String CRITERIA_ACTIVITY_ORG_ID = "organizationId";
  public static final String CRITERIA_ACTIVITY_ORG_NAME = "organizationName";
  public static final String CRITERIA_DETAILS = "details";
  
  private ActivityCriteriaConstant() {
    //static only
  }
}
