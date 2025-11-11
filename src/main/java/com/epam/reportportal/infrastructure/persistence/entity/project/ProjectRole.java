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

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Ivan Budayeu
 */
public enum ProjectRole implements Comparable<ProjectRole> {

  VIEWER(0, "Viewer"),
  EDITOR(1, "Editor");

  private final int roleLevel;
  private final String roleName;

  ProjectRole(int level, String roleName) {
    this.roleLevel = level;
    this.roleName = roleName;
  }

  public boolean higherThan(ProjectRole other) {
    return this.roleLevel > other.roleLevel;
  }

  public boolean lowerThan(ProjectRole other) {
    return this.roleLevel < other.roleLevel;
  }

  public boolean sameOrHigherThan(ProjectRole other) {
    return this.roleLevel >= other.roleLevel;
  }

  public boolean sameOrLowerThan(ProjectRole other) {
    return this.roleLevel <= other.roleLevel;
  }

  public static Optional<ProjectRole> forName(final String name) {
    return Arrays.stream(ProjectRole.values()).filter(role -> role.name().equalsIgnoreCase(name))
        .findAny();
  }

  public String getRoleName() {
    return roleName;
  }

}
