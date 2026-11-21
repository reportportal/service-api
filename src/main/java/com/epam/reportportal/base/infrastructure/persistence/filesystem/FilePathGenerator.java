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

package com.epam.reportportal.base.infrastructure.persistence.filesystem;


import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.AttachmentMetaInfo;
import com.epam.reportportal.base.infrastructure.persistence.util.DateTimeProvider;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Builds project-scoped file paths for binary and attachment storage.
 *
 * @author Dzianis_Shybeka
 */
@Component
public class FilePathGenerator {

  private static final Pattern UNSAFE_PATH_SEGMENT_CHARS = Pattern.compile("[^0-9a-zA-Z_-]");

  private final DateTimeProvider dateTimeProvider;

  public FilePathGenerator(DateTimeProvider dateTimeProvider) {
    this.dateTimeProvider = dateTimeProvider;
  }

  /**
   * Generate relative file path for new local file. projectId/year-month/launchUUID
   *
   * @return Generated path
   */
  public String generate(AttachmentMetaInfo metaInfo) {
    LocalDateTime localDateTime = dateTimeProvider.localDateTimeNow();
    String date = localDateTime.getYear() + "-" + localDateTime.getMonthValue();
    return Paths.get(String.valueOf(metaInfo.getProjectId()), date,
        sanitizePathSegment(metaInfo.getLaunchUuid())).toString();
  }

  /**
   * Makes a client-supplied value (e.g. launch or log UUID) safe to use as a single path segment:
   * every character other than letters, digits, '_' and '-' is replaced with '_', so the value can
   * contain neither separators nor "..". Regular UUIDs are returned unchanged.
   *
   * @param value Client-supplied value
   * @return Value safe to use as one path segment, or {@code null} for {@code null}
   */
  public static String sanitizePathSegment(String value) {
    return value == null ? null : UNSAFE_PATH_SEGMENT_CHARS.matcher(value).replaceAll("_");
  }
}
