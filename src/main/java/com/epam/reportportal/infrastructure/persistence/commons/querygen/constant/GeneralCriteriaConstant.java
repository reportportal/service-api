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

package com.epam.reportportal.infrastructure.persistence.commons.querygen.constant;

/**
 * General search criteria fields.
 *
 * @author Anton Machulski
 */
public final class GeneralCriteriaConstant {

  public static final String CRITERIA_ID = "id";

  public static final String CRITERIA_UUID = "uuid";
  public static final String CRITERIA_NAME = "name";
  public static final String CRITERIA_SHARED = "shared";
  public static final String CRITERIA_OWNER = "owner";
  public static final String CRITERIA_PROJECT_ID = "projectId";
  public static final String CRITERIA_USER_ID = "userId";
  public static final String CRITERIA_CREATION_DATE = "creationDate";
  public static final String CRITERIA_LAST_MODIFIED = "lastModified";
  public static final String CRITERIA_DESCRIPTION = "description";
  public static final String CRITERIA_PROJECT = "project";
  public static final String CRITERIA_LAUNCH_ID = "launchId";
  public static final String CRITERIA_START_TIME = "startTime";
  public static final String CRITERIA_END_TIME = "endTime";
  public static final String CRITERIA_SLUG = "slug";


  private GeneralCriteriaConstant() {
    //static only
  }
}
