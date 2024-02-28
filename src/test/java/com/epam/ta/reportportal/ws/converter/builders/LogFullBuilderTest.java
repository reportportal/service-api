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

package com.epam.ta.reportportal.ws.converter.builders;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogFullBuilderTest {

  @Test
  void logBuilder() {
    final SaveLogRQ createLogRQ = new SaveLogRQ();
    final String message = "message";
    createLogRQ.setMessage(message);
    createLogRQ.setLevel("ERROR");
    final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

    createLogRQ.setLogTime(TO_DATE.apply(now));
    TestItem item = new TestItem();
    item.setItemId(1L);
    item.setUniqueId("uuid");

    final LogFull logFull = new LogFullBuilder().addSaveLogRq(createLogRQ).addTestItem(item).get();

    assertEquals(message, logFull.getLogMessage());
    assertEquals(40000, (int) logFull.getLogLevel());
    assertEquals(now, logFull.getLogTime());
    assertThat(logFull.getTestItem()).isEqualToComparingFieldByField(item);
  }
}