/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.epam.reportportal.base.infrastructure.persistence.entity.activity;

import java.util.Arrays;
import java.util.Optional;

public enum EventAction {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  BULK_CREATE("bulkCreate"),
  BULK_UPDATE("bulkUpdate"),
  BULK_DELETE("bulkDelete"),
  ANALYZE("analyze"),
  START("start"),
  FINISH("finish"),
  POST("post"),
  LINK("link"),
  UNLINK("unlink"),
  ASSIGN("assign"),
  UNASSIGN("unassign"),
  GENERATE("generate"),
  MATCH("match"),
  CHANGE_ROLE("changeRole"),
  UPDATE_USER_ROLE("updateUserRole");

  private final String value;

  EventAction(String value) {
    this.value = value;
  }

  public static Optional<EventAction> fromString(String string) {
    return Optional.ofNullable(string)
        .flatMap(str -> Arrays.stream(values())
            .filter(it -> it.value.equalsIgnoreCase(str))
            .findAny());
  }

  public String getValue() {
    return value;
  }

}
