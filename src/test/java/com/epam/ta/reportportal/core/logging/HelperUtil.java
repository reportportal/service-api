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

package com.epam.ta.reportportal.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class HelperUtil {

	public static void checkLoggingRecords(Appender<ILoggingEvent> appender, int numberOfRecords, Level[] levels, String ... records) {
		ArgumentCaptor<LoggingEvent> argument = ArgumentCaptor.forClass(LoggingEvent.class);
		verify(appender, times(numberOfRecords)).doAppend(argument.capture());

		List<LoggingEvent> events = argument.getAllValues();
		for (int i = 0; i < events.size(); i++) {
			assertEquals(levels[i], events.get(i).getLevel());
			assertEquals(orderedMultilineString(records[i]), orderedMultilineString(events.get(i).getMessage()));
		}
	}

	public static String orderedMultilineString(String input) {
		String[] lines = input.split("\n");
		Arrays.sort(lines);
		return Stream.of(lines).collect(Collectors.joining("\n"));
	}
}
