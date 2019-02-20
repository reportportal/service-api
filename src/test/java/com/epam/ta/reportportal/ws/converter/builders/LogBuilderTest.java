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