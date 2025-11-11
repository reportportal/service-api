/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.core.item.identity;

import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class IdentityUtil {

  private IdentityUtil() {
    //static only
  }

  /**
   * Parse {@link TestItem#getPath()} and get all ids excluding id of the provided {@link TestItem}
   *
   * @param testItem {@link TestItem}
   * @return {@link List} with ids parsed from {@link TestItem#getPath()}
   */
  public static List<Long> getParentIds(TestItem testItem) {
    return getIds(testItem.getPath(), false);
  }

  /**
   * * Parse {@link TestItem#getPath()} and get all ids including id of the provided {@link TestItem}
   *
   * @param testItem {@link TestItem}
   * @return {@link List} with ids parsed from {@link TestItem#getPath()}
   */
  public static List<Long> getItemTreeIds(TestItem testItem) {
    return getIds(testItem.getPath(), true);
  }

  /**
   * * Parse {@link TestItem#getPath()} and get all ids including id of the provided {@link TestItem}
   *
   * @param path {@link TestItem#getPath()}
   * @return {@link List} with ids parsed from {@link TestItem#getPath()}
   */
  public static List<Long> getItemTreeIds(String path) {
    return getIds(path, true);
  }

  private static List<Long> getIds(String path, boolean includeLast) {
    String[] ids = path.split("\\.");
    return Stream.of(ids).limit(includeLast ? ids.length : ids.length - 1).map(id -> {
      try {
        return Long.parseLong(id);
      } catch (NumberFormatException e) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Incorrect path value: " + id);
      }
    }).collect(Collectors.toList());
  }
}
