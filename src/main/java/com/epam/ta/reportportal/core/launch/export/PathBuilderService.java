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

package com.epam.ta.reportportal.core.launch.export;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

/**
 * Service responsible for building safe file paths and folder structures
 * for use inside ZIP archives.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class PathBuilderService {

  /**
   * Builds a full file path for an attachment within a test item's folder.
   *
   * @param basePath the base path (e.g., item folder)
   * @param fileName the original file name
   * @return a sanitized path safe for ZIP storage
   */
  public String buildAttachmentPath(String basePath, String fileName) {
    return basePath + "/" + sanitize(fileName);
  }

  /**
   * Constructs a full hierarchical path for a test item based on its parent chain.
   *
   * @param items the full map of test item IDs to test items
   * @param item  the test item whose path should be built
   * @return a sanitized path for the item (e.g., "Suite/TestCase/Step")
   */
  public String buildItemPath(Map<Long, TestItemPojo> items, TestItemPojo item) {
    return getPathNames(items, item).stream()
        .map(this::sanitize)
        .collect(Collectors.joining("/"));
  }

  private List<String> getPathNames(Map<Long, TestItemPojo> items, TestItemPojo current) {
    if (Strings.isBlank(current.getPath())) {
      return Collections.emptyList();
    }
    return Arrays.stream(current.getPath().split("\\."))
        .map(Long::parseLong)
        .map(id -> items.get(id).getItemName())
        .collect(Collectors.toList());
  }

  /**
   * Sanitizes file or folder names by removing invalid or unsafe characters.
   *
   * @param name the original name
   * @return the sanitized version
   */
  private String sanitize(String name) {
    if (name == null || name.isBlank()) {
      return "unknown";
    }
    return name.replaceAll("[\\\\/:*?\"<>|]", "_")
        .replaceAll("[\\p{Cntrl}]", "")
        .replaceAll("\\s+", " ")
        .trim();
  }


}
