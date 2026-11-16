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

package com.epam.reportportal.base.infrastructure.persistence.util;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * User-related helpers, such as email format validation.
 *
 * @author Reingold Shekhtel
 */
public final class UserUtils {

  private UserUtils() {
    //static only
  }

  /**
   * Validate email format against <a href="http://www.ietf.org/rfc/rfc822.txt" target="_top">RFC822</a>.
   *
   * @param email Email to be validated
   * @return TRUE of email is valid
   */
  public static boolean isEmailValid(String email) {
    try {
      var internetAddress = new InternetAddress(email);
      internetAddress.validate();
      return true;
    } catch (AddressException e) {
      return false;
    }
  }
}
