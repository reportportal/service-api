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
 * @author Ivan Budaev
 */
public final class UserCriteriaConstant {

  public static final String CRITERIA_EXTERNALID = "externalId"; // TODO: for removal
  public static final String CRITERIA_EXTERNAL_ID = "external_id";

  public static final String CRITERIA_ACTIVE = "active";

  public static final String CRITERIA_USER = "user";
  public static final String CRITERIA_ROLE = "role";
  public static final String CRITERIA_INSTANCE_ROLE = "instance_role"; // TODO: for removal
  public static final String CRITERIA_TYPE = "type"; // TODO: for removal
  public static final String CRITERIA_ACCOUNT_TYPE = "account_type";
  public static final String CRITERIA_FULL_NAME = "fullName"; // TODO: for removal
  public static final String CRITERIA_FULLNAME = "full_name";
  public static final String CRITERIA_EMAIL = "email";
  public static final String CRITERIA_EXPIRED = "expired";
  public static final String CRITERIA_LAST_LOGIN = "lastLogin";
  public static final String CRITERIA_SYNCHRONIZATION_DATE = "synchronizationDate";
  public static final String CRITERIA_USER_PROJECT = "project";
  public static final String CRITERIA_USER_CREATED_AT = "created_at";
  public static final String CRITERIA_USER_UPDATED_AT = "updated_at";
  public static final String CRITERIA_USER_ORGANIZATION_ID = "org_id";


  private UserCriteriaConstant() {
    //static only
  }
}
