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

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public class LogBuilder implements Supplier<Log> {

	private final Log log;

	public LogBuilder() {
		log = new Log();
	}

	public LogBuilder addSaveLogRq(SaveLogRQ createLogRQ) {
		log.setLogLevel(LogLevel.toCustomLogLevel(createLogRQ.getLevel()));
		log.setLogMessage(ofNullable(createLogRQ.getMessage()).orElse("NULL"));
		log.setLogTime(EntityUtils.TO_LOCAL_DATE_TIME.apply(createLogRQ.getLogTime()));
		log.setUuid(ofNullable(createLogRQ.getUuid()).orElse(UUID.randomUUID().toString()));
		return this;
	}

	public LogBuilder addTestItem(TestItem testItem) {
		log.setTestItem(testItem);
		return this;
	}

	public LogBuilder addLaunch(Launch launch) {
		log.setLaunch(launch);
		return this;
	}

	public LogBuilder addProjectId(Long projectId) {
		log.setProjectId(projectId);
		return this;
	}

	@Override
	public Log get() {
		return log;
	}

}
