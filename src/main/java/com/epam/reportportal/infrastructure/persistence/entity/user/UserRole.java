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

package com.epam.reportportal.infrastructure.persistence.entity.user;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * UserRole representation<br> Role has more rights than the following one. So, Administrator is more privileged than
 * User.
 *
 * @author Andrei Varabyeu
 */
public enum UserRole {

  USER,
  ADMINISTRATOR;

  public static final String ROLE_PREFIX = "ROLE_";

  public static Optional<UserRole> findByName(String name) {
    return Arrays.stream(UserRole.values()).filter(role -> role.name().equals(name)).findAny();
  }

  public static Optional<UserRole> findByAuthority(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return Optional.empty();
    }
    return findByName(StringUtils.substringAfter(name, ROLE_PREFIX));
  }

  public String getAuthority() {
    return "ROLE_" + this.name();
  }

}
