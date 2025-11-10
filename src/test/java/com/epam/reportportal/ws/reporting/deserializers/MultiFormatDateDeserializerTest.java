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

package com.epam.reportportal.reporting.deserializers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.epam.reportportal.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class MultiFormatDateDeserializerTest {

  private static final Instant expectedTime = Instant.parse("2024-03-01T20:24:09.930987Z");
  private static final Instant expectedTimeMillis = Instant.parse("2024-03-01T20:24:09.930Z");

  @Mock
  JsonParser jsonParser;

  @ParameterizedTest
  @ValueSource(strings = {
      "2024-03-01T20:24:09.930987Z",
      "2024-03-01T20:24:09.930987654",
      "2024-03-01T20:24:09.930987+0000",
      "2024-03-01T20:24:09.930987999"
  })
  void deserializeDatesMicros(String strDate) throws IOException {
    MultiFormatDateDeserializer a = new MultiFormatDateDeserializer();
    when(jsonParser.getText()).thenReturn(strDate);
    Instant date = a.deserialize(jsonParser, mock(DeserializationContext.class));

    Assertions.assertEquals(expectedTime, date.truncatedTo(ChronoUnit.MICROS));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "2024-03-01T20:24:09.930Z",
      "2024-03-01T20:24:09.930",
      "2024-03-01T20:24:09.930+00:00",
      "2024-03-01T19:24:09.930-01:00",
      "1709324649930"
  })
  void deserializeDatesMillis(String strDate) throws IOException {
    MultiFormatDateDeserializer a = new MultiFormatDateDeserializer();
    when(jsonParser.getText()).thenReturn(strDate);
    Instant date = a.deserialize(jsonParser, mock(DeserializationContext.class));

    Assertions.assertEquals(expectedTimeMillis, date.truncatedTo(ChronoUnit.MICROS));
  }


  @ParameterizedTest
  @ValueSource(longs = {
      1709324649930L,
  })
  void deserializeIntegerFormat(Long longDate) throws IOException {
    MultiFormatDateDeserializer a = new MultiFormatDateDeserializer();
    when(jsonParser.getLongValue()).thenReturn(longDate);
    Instant date = a.deserialize(jsonParser, mock(DeserializationContext.class));

    Assertions.assertEquals(expectedTimeMillis, date.truncatedTo(ChronoUnit.MICROS));
  }
}
