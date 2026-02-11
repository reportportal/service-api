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

package com.epam.reportportal.auth.integration.saml;

/**
 * Used for population of user settings from SAML response
 *
 * @author Yevgeniy Svalukhin
 */
public enum UserAttribute {
  FIRST_NAME("FirstName"),
  LAST_NAME("LastName"),
  EMAIL("Email"),
  ROLES("Roles");

  private final String value;

  UserAttribute(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
