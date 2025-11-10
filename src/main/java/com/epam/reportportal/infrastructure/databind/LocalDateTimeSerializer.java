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

package com.epam.reportportal.infrastructure.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Serialization class for converting dates into array of integers. Required for compatibility with analyzer.
 *
 * @author Siarhei Hrabko
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

  @Override
  public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    int[] time = {
        localDateTime.getYear(),
        localDateTime.getMonthValue(),
        localDateTime.getDayOfMonth(),
        localDateTime.getHour(),
        localDateTime.getMinute(),
        localDateTime.getSecond(),
        localDateTime.getNano()
    };
    jsonGenerator.writeArray(time, 0, 7);
  }
}
