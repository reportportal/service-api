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

import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogBuilderTest {

	@Test
	void logBuilder() {
		final SaveLogRQ createLogRQ = new SaveLogRQ();
		final String message = "message";
		createLogRQ.setMessage(message);
		createLogRQ.setLevel("ERROR");
		final LocalDateTime now = LocalDateTime.now();
		createLogRQ.setLogTime(Date.from(now.atZone(ZoneId.of("UTC")).toInstant()));
		TestItem item = new TestItem();
		item.setItemId(1L);
		item.setUniqueId("uuid");

		final Log log = new LogBuilder().addSaveLogRq(createLogRQ).addTestItem(item).get();

		assertEquals(message, log.getLogMessage());
		assertEquals(40000, (int) log.getLogLevel());
		assertEquals(now, log.getLogTime());
		assertThat(log.getTestItem()).isEqualToComparingFieldByField(item);
	}
}