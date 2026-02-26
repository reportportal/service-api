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

package com.epam.reportportal.base.infrastructure.persistence.entity.organization;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.OrganizationType;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Siarhei Hrabko
 */
@Getter
@Setter
@NoArgsConstructor
public class OrganizationFilter implements Serializable {

  public static final String USERS_QUANTITY = "usersQuantity";
  public static final String LAUNCHES_QUANTITY = "launchesQuantity";
  public static final String PROJECTS_QUANTITY = "projectsQuantity";
  public static final String LAST_RUN = "lastRun";

  private Long id;
  private Instant createdAt;
  private Instant updatedAt;
  private String name;
  private String externalId;
  private OrganizationType organizationType;
  private String slug;

  private int usersQuantity;
  private int projectsQuantity;
  private int launchesQuantity;

  private Instant lastRun;

}
