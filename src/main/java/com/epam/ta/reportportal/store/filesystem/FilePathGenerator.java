/*
 *
 *  * Copyright (C) 2018 EPAM Systems
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.epam.ta.reportportal.store.filesystem;

import com.epam.ta.reportportal.util.DateTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author Dzianis_Shybeka
 */
@Component
public class FilePathGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(FilePathGenerator.class);

	private final DateTimeProvider dateTimeProvider;

	public FilePathGenerator(DateTimeProvider dateTimeProvider) {
		this.dateTimeProvider = dateTimeProvider;
	}

	/**
	 * Generate relative file path for new local file. ${Day of the year}/${split UUID part}
	 *
	 * @return
	 */
	public String generate() {

		String uuid = UUID.randomUUID().toString();

		int dayOfYear = dateTimeProvider.localDateTimeNow().getDayOfYear();

		String levelOne = uuid.substring(0, 2);
		String levelTwo = uuid.substring(2, 4);
		String levelThree = uuid.substring(4, 6);
		String tail = uuid.substring(6);

		String result = Paths.get(String.valueOf(dayOfYear), levelOne, levelTwo, levelThree, tail).toString();

		LOG.debug("File path generated: {}", result);

		return result;
	}
}
