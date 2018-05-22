package com.epam.ta.reportportal.store.database.mappers;

import com.epam.ta.reportportal.store.database.entity.log.Log;
import org.jooq.Record;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.epam.ta.reportportal.store.jooq.Tables.LOG;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogMapperTest {

	private LogMapper logMapper;

	@Before
	public void setUp() throws Exception {

		logMapper = new LogMapper();
	}

	@Test
	public void testGetLog() {

		//given:
		Record record = mock(Record.class);

		Long id = 12L;
		Integer level = 11;
		String message = "message";
		Timestamp lastModified = new Timestamp(System.currentTimeMillis());
		Timestamp logTime = new Timestamp(System.currentTimeMillis());

		//setup:
		when(record.getValue(LOG.ID)).thenReturn(id);
		when(record.getValue(LOG.LOG_LEVEL)).thenReturn(level);
		when(record.getValue(LOG.LOG_MESSAGE)).thenReturn(message);
		when(record.getValue(LOG.LAST_MODIFIED)).thenReturn(lastModified);
		when(record.getValue(LOG.LOG_TIME)).thenReturn(logTime);

		//when:
		Log log = LogMapper.getLog(record);

		//then:
		assertEquals(id, log.getId());
		assertEquals(level, log.getLogLevel());
		assertEquals(message, log.getLogMessage());
		assertEquals(LocalDateTime.ofInstant(lastModified.toInstant(), ZoneOffset.UTC), log.getLastModified());
		assertEquals(LocalDateTime.ofInstant(logTime.toInstant(), ZoneOffset.UTC), log.getLogTime());
	}
}