/*
 * Copyright 2024 EPAM Systems
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
 * Search criteria fields for Organization.
 *
 * @author Siarhei Hrabko
 */
public final class OrganizationCriteriaConstant {

  public static final String CRITERIA_ORG_ID = "organization_id";
  public static final String CRITERIA_ORG_NAME = "name";
  public static final String CRITERIA_ORG_SLUG = "slug";
  public static final String CRITERIA_ORG_TYPE = "type";
  public static final String CRITERIA_ORG_CREATED_AT = "created_at";
  public static final String CRITERIA_ORG_UPDATED_AT = "updated_at";
  public static final String CRITERIA_ORG_USERS = "users";
  public static final String CRITERIA_ORG_USER_ID = "org_user_id";
  public static final String CRITERIA_ORG_USER_ROLE = "organization_role";
  public static final String CRITERIA_ORG_PROJECTS = "projects";
  public static final String CRITERIA_ORG_LAUNCHES = "launches";
  public static final String CRITERIA_ORG_LAST_LAUNCH_RUN = "last_launch_occurred";

  private OrganizationCriteriaConstant() {
    //static only
  }
}
