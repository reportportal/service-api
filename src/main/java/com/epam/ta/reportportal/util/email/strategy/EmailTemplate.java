/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.util.email.strategy;

import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Andrei Piankouski
 */
public enum EmailTemplate {

  USER_EXPIRATION_NOTIFICATION("userExpirationNotification"),
  USER_DELETION_NOTIFICATION("userDeletionNotification");

  private final String name;

  EmailTemplate(String name) {
    this.name = name;
  }

  public static EmailTemplate fromString(String type) {
    return Arrays.stream(EmailTemplate.values())
        .filter(it -> it.getName().equalsIgnoreCase(type))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.INCORRECT_REQUEST,
            "Incorrect analyzer type. Allowed are: " + Arrays.stream(EmailTemplate.values())
                .map(EmailTemplate::getName)
                .collect(Collectors.toList())
        ));
  }

  public String getName() {
    return name;
  }
}
