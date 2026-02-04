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
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public final class ProjectCriteriaConstant {

  public static final String CRITERIA_PROJECT_TYPE = "type";
  public static final String CRITERIA_PROJECT_NAME = "name";
  public static final String CRITERIA_PROJECT_KEY = "key";
  public static final String CRITERIA_PROJECT_SLUG = "slug";
  public static final String CRITERIA_ALLOCATED_STORAGE = "allocatedStorage";
  public static final String CRITERIA_PROJECT_ORGANIZATION = "organization";
  public static final String CRITERIA_PROJECT_ORGANIZATION_ID = "organization_id";
  public static final String CRITERIA_PROJECT_CREATION_DATE = "creationDate";
  public static final String CRITERIA_PROJECT_ATTRIBUTE_NAME = "attributeName";

  private ProjectCriteriaConstant() {
    //static only
  }
}
