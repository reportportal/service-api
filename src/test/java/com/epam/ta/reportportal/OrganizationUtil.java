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

package com.epam.ta.reportportal;

import com.epam.ta.reportportal.entity.enums.OrganizationType;
import com.epam.ta.reportportal.entity.organization.Organization;
import java.time.LocalDateTime;

/**
 * @author Siarhei Hrabko
 */
public class OrganizationUtil {

  public static final String TEST_ORG_NAME = "Org Name";
  public static final String TEST_PROJECT_NAME = "project Name";

  public static final Long TEST_ORG_ID = 1L;
  public static final String TEST_ORG_SLUG = "o-slug";
  public static final String TEST_PROJECT_SLUG = "project-name";
  public static final String TEST_PROJECT_KEY = TEST_ORG_SLUG + "." + TEST_PROJECT_SLUG;
  public static final Organization TEST_ORG = new Organization(TEST_ORG_ID,
      LocalDateTime.now(),
      TEST_ORG_NAME,
      OrganizationType.INTERNAL,
      TEST_ORG_SLUG);

  private OrganizationUtil() {
  }

}
