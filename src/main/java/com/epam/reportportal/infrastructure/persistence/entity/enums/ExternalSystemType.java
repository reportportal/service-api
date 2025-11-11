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

package com.epam.reportportal.infrastructure.persistence.entity.enums;

import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * List of supported external systems
 *
 * @author Andrei Varabyeu
 */
public enum ExternalSystemType {

  NONE {
    @Override
    public String makeUrl(String base, String id) {
      return null;
    }
  },
  JIRA {
    @Override
    public String makeUrl(String base, String id) {
      return StringUtils.stripEnd(base, "/") + "/browse/" + id;
    }
  },
  TFS {
    @Override
    public String makeUrl(String base, String id) {
      return StringUtils.stripEnd(base, "/") + "/browse/" + id;
    }
  },
  RALLY {
    @Override
    public String makeUrl(String base, String id) {
      return "";
    }
  };

  public static final String ISSUE_MARKER = "#";

  ExternalSystemType() {

  }

  public static Optional<String> knownIssue(String summary) {
    if (summary.trim().startsWith(ISSUE_MARKER)) {
      return Optional.of(StringUtils.substringAfter(summary, ISSUE_MARKER));
    } else {
      return Optional.empty();
    }
  }

  public static Optional<ExternalSystemType> findByName(String name) {
    return Arrays.stream(ExternalSystemType.values())
        .filter(type -> type.name().equalsIgnoreCase(name)).findAny();
  }

  public static boolean isPresent(String name) {
    return findByName(name).isPresent();
  }

  public abstract String makeUrl(String base, String id);
}
