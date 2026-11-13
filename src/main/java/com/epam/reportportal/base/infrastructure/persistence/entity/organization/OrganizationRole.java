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

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

/**
 * Organization-level roles (e.g. member, manager) and their ordering.
 *
 * @author Siarhei Hrabko
 */
@Getter
public enum OrganizationRole implements Comparable<OrganizationRole> {

  MEMBER(0, "Member"),
  MANAGER(1, "Manager");


  private final int roleLevel;
  private final String roleName;

  OrganizationRole(int level, String roleName) {
    this.roleLevel = level;
    this.roleName = roleName;
  }

  public static Optional<OrganizationRole> forName(final String name) {
    return Arrays.stream(OrganizationRole.values())
        .filter(role -> role.name().equalsIgnoreCase(name))
        .findAny();
  }

  public boolean higherThan(OrganizationRole other) {
    return this.roleLevel > other.roleLevel;
  }

  public boolean lowerThan(OrganizationRole other) {
    return this.roleLevel < other.roleLevel;
  }

  public boolean sameOrHigherThan(OrganizationRole other) {
    return this.roleLevel >= other.roleLevel;
  }

  public boolean sameOrLowerThan(OrganizationRole other) {
    return this.roleLevel <= other.roleLevel;
  }

}
