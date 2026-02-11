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

package com.epam.reportportal.base.infrastructure.persistence.commons;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;

import com.google.common.base.Preconditions;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Some useful utils for working with entities<br> For example: usernames, project names, tags, etc.
 *
 * @author Andrei Varabyeu
 */
public class EntityUtils {

  public static final Function<Date, LocalDateTime> TO_LOCAL_DATE_TIME = date -> ofNullable(
      date).map(d -> LocalDateTime.ofInstant(d.toInstant(),
      ZoneOffset.UTC
  )).orElse(null);

  public static final Function<LocalDateTime, Date> TO_DATE = localDateTime -> ofNullable(
      localDateTime).map(l -> Date.from(l.atZone(
      ZoneOffset.UTC).toInstant())).orElse(null);

  public static final Function<Instant, LocalDateTime> INSTANT_TO_LDT = instant ->
      ofNullable(instant)
          .map(i -> i.atZone(ZoneOffset.UTC).toLocalDateTime())
          .orElse(null);

  public static final Function<Instant, Timestamp> INSTANT_TO_TIMESTAMP = instant ->
      ofNullable(instant)
          .map(i -> i.atZone(ZoneOffset.UTC).toLocalDateTime())
          .map(Timestamp::valueOf)
          .orElse(null);
  /**
   * Remove leading and trailing spaces from list of string
   */
  public static final Function<String, String> TRIM_FUNCTION = it -> ofNullable(it).map(
      String::trim).orElse(null);
  public static final Predicate<String> NOT_EMPTY = s -> !isNullOrEmpty(s);
  private static final String OLD_SEPARATOR = ",";
  private static final String NEW_SEPARATOR = "_";
  /**
   * Convert declined symbols on allowed for WS and UI
   */
  public static final Function<String, String> REPLACE_SEPARATOR = s -> ofNullable(s).map(
          it -> it.replace(OLD_SEPARATOR, NEW_SEPARATOR))
      .orElse(null);

  private EntityUtils() {
    //static only
  }

  /**
   * Normalize any ID for database ID fields, for example
   *
   * @param id ID to normalize
   * @return String
   */

  public static String normalizeId(String id) {
    return Preconditions.checkNotNull(id, "Provided value shouldn't be null").toLowerCase();
  }

}
