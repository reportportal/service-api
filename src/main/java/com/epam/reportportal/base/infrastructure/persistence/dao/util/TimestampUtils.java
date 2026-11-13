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

package com.epam.reportportal.base.infrastructure.persistence.dao.util;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

/**
 * SQL timestamp utilities relative to the current time.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public final class TimestampUtils {

  private TimestampUtils() {
    //static only
  }

  public static Timestamp getTimestampBackFromNow(Duration period) {
    return Timestamp.from(Instant.now().minusSeconds(period.getSeconds()));
  }

  public static Instant getInstantBackFromNow(Duration period) {
    return Instant.now().minusSeconds(period.getSeconds());
  }
}
