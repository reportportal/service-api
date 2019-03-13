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

package com.epam.ta.reportportal.ws.converter;

import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogResourceAssemblerTest {

	@Test
	void toResource() {
		LogResourceAssembler resourceAssembler = new LogResourceAssembler();
		Log log = getLog();
		LogResource logResource = resourceAssembler.toResource(log);

		assertEquals(logResource.getId(), log.getId());
		assertEquals(logResource.getLevel(), LogLevel.toLevel(log.getLogLevel()).name());
		assertEquals(logResource.getMessage(), log.getLogMessage());
		assertEquals(logResource.getTestItem(), String.valueOf(log.getTestItem().getItemId()));
	}

	@Test
	void toResources() {
		LogResourceAssembler resourceAssembler = new LogResourceAssembler();
		List<LogResource> logResources = resourceAssembler.toResources(Collections.singleton(getLog()));

		assertEquals(1, logResources.size());
	}

	@Test
	void apply() {
		LogResourceAssembler resourceAssembler = new LogResourceAssembler();
		Log log = getLog();
		LogResource logResource = resourceAssembler.apply(log);

		assertEquals(logResource.getId(), log.getId());
		assertEquals(logResource.getLevel(), LogLevel.toLevel(log.getLogLevel()).name());
		assertEquals(logResource.getMessage(), log.getLogMessage());
		assertEquals(logResource.getTestItem(), String.valueOf(log.getTestItem().getItemId()));
	}

	private Log getLog() {
		Log log = new Log();
		log.setId(1L);
		log.setLogTime(LocalDateTime.now());
		log.setLastModified(LocalDateTime.now());
		log.setLogMessage("message");
		log.setLogLevel(40000);
		TestItem testItem = new TestItem();
		testItem.setItemId(2L);
		log.setTestItem(testItem);

		return log;
	}
}