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
