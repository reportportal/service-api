/*
 * Copyright 2024 EPAM Systems
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

package com.epam.ta.reportportal.util;

import static com.epam.ta.reportportal.ws.converter.builders.UserBuilder.USER_LAST_LOGIN;
import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.entity.Metadata;
import java.time.Instant;

public class MetadataUtils {

  public static final String ATTACHMENT_CONTENT_TYPE = "attachmentContentType";

  private MetadataUtils() {
    //static only
  }

  /**
   * Retrieves the last login time from the given metadata.
   *
   * @param meta the metadata object containing user information
   * @return the last login time as an {@link Instant}, or null if not present
   */
  public static Instant getLastLogin(Metadata meta) {
    return ofNullable(meta)
        .map(Metadata::getMetadata)
        .map(metadata -> metadata.get(USER_LAST_LOGIN))
        .map(String::valueOf)
        .map(Long::parseLong)
        .map(Instant::ofEpochMilli)
        .orElse(null);
  }

  public static String getMediaType(Metadata meta) {
    return ofNullable(meta)
        .map(Metadata::getMetadata)
        .map(metadata -> metadata.get(ATTACHMENT_CONTENT_TYPE))
        .map(String::valueOf)
        .orElse(null);
  }
}
