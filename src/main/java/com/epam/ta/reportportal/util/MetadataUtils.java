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

  private MetadataUtils() {
    //static only
  }

  public static Instant getLastLogin(Metadata meta) {
    return ofNullable(meta)
        .map(Metadata::getMetadata)
        .map(metadata -> (Long) metadata.get(USER_LAST_LOGIN))
        .map(Instant::ofEpochMilli)
        .orElse(null);
  }
}
