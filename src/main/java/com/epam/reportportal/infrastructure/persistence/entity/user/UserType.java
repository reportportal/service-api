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

import java.util.Arrays;
import java.util.Optional;

/**
 * User Type enumeration<br> Used for supporting different project types processing
 *
 * @author <a href="mailto:andrei_varabyeu@epam.com">Andrei Varabyeu</a>
 */
public enum UserType {

  //@formatter:off
  INTERNAL,
  UPSA,
  GITHUB,
  LDAP,
  SAML,

  SCIM;
  //@formatter:on

  public static UserType getByName(String type) {
    return UserType.valueOf(type);
  }

  public static Optional<UserType> findByName(String name) {
    return Arrays.stream(UserType.values()).filter(type -> type.name().equalsIgnoreCase(name))
        .findAny();
  }

  public static boolean isPresent(String name) {
    return findByName(name).isPresent();
  }
}
