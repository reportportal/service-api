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

package com.epam.reportportal.base.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Project Type enumeration<br> Used for supporting different organization types processing.
 *
 * @author Siarhei Hrabko
 */
public enum OrganizationType {

  PERSONAL,
  INTERNAL,
  EXTERNAL;

  public static Optional<OrganizationType> findByName(String name) {
    return Arrays.stream(OrganizationType.values())
        .filter(type -> type.name().equalsIgnoreCase(name))
        .findAny();
  }

  public static boolean isPresent(String name) {
    return findByName(name).isPresent();
  }
}
