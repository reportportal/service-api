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

package com.epam.reportportal.infrastructure.persistence.entity.project;

import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Not database object. Representation of the result of project info query
 *
 * @author Pavel Bortnik
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfo implements Serializable {

  public static final String USERS_QUANTITY = "usersQuantity";
  public static final String LAUNCHES_QUANTITY = "launchesQuantity";
  public static final String LAST_RUN = "lastRun";

  private Long id;

  private Instant creationDate;

  private String name;

  private String projectType;

  private String organizationId;

  private String key;

  private String slug;

  private int usersQuantity;

  private int launchesQuantity;

  private Instant lastRun;

}
