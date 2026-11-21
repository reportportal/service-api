/*
 * Copyright 2024 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.reporting.databind;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserialization class for parsing incoming dates of different formats.
 *
 * @author Siarhei Hrabko
 */
public class MultiFormatDateDeserializer extends ValueDeserializer<Instant> {

  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

  private static final DateTimeFormatter ZONE_OFFSET_FORMAT = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
      .withZone(ZoneOffset.UTC);

  private static final DateTimeFormatter LOCAL_DATE_TIME_MS_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private static final DateTimeFormatter LOCAL_DATE_TIME_MS_FORMAT_DATE =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

  private static final List<DateTimeFormatter> PREDEFINED_FORMATS = Arrays.asList(
      DateTimeFormatter.RFC_1123_DATE_TIME,
      DateTimeFormatter.ISO_OFFSET_DATE_TIME,
      DateTimeFormatter.ISO_DATE_TIME,
      DateTimeFormatter.ISO_LOCAL_DATE_TIME,
      TIMESTAMP_FORMAT,
      LOCAL_DATE_TIME_MS_FORMAT,
      LOCAL_DATE_TIME_MS_FORMAT_DATE,
      ZONE_OFFSET_FORMAT
  );

  @Override
  public Instant deserialize(JsonParser parser, DeserializationContext context) {
    try {
      long longDate = parser.getLongValue();
      if (parser.getString() == null) {
        return getTruncatedToMicros(longDate);
      }
    } catch (Exception _) {
      // ignore
    }
    try {
      long longDate = Long.parseLong(parser.getString());
      return getTruncatedToMicros(longDate);
    } catch (Exception _) {
      // ignore
    }

    String strDate = parser.getString();

    for (DateTimeFormatter formatter : PREDEFINED_FORMATS) {
      try {
        TemporalAccessor parsedDate = formatter.parseBest(strDate, ZonedDateTime::from,
            LocalDateTime::from);
        Instant instant =
            parsedDate instanceof ZonedDateTime zonedDateTime ? zonedDateTime.toInstant()
                : ((LocalDateTime) parsedDate).toInstant(ZoneOffset.UTC);
        return instant.truncatedTo(ChronoUnit.MICROS);
      } catch (DateTimeParseException _) {
        // Exception means the text could not be parsed with this formatter, continue with next formatter
      }
    }
    throw DatabindException.from(parser, "Unable to parse date: " + strDate);
  }

  private Instant getTruncatedToMicros(long longDate) {
    return Instant.ofEpochMilli(longDate).truncatedTo(ChronoUnit.MICROS);
  }
}
