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

package com.epam.reportportal.base.infrastructure.persistence.dao.constant;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class TestConstants {

  public static final Long SUPERADMIN_ID = 1L;
  public static final Long SUPERADMIN_PERSONAL_PROJECT_ID = 1L;
  public static final Long DEFAULT_PERSONAL_PROJECT_ID = 2L;
  public static final String SUPERADMIN_LOGIN = "superadmin";
  public static final Long STEP_ITEM_WITH_LOGS_ID = 1L;
  public static final Long RALLY_INTEGRATION_TYPE_ID = 2L;
  public static final Long JIRA_INTEGRATION_TYPE_ID = 3L;
  public static final Long EMAIL_INTEGRATION_TYPE_ID = 4L;
  public static final Long GLOBAL_EMAIL_INTEGRATION_ID = 17L;
  public static final Long RALLY_INTEGRATION_ID = 1L;
  public static final Long JIRA_INTEGRATION_ID = 2L;

  private TestConstants() {
    //static only
  }
}
