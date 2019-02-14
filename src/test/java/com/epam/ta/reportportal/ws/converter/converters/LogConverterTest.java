package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class LogConverterTest {

	@Test
	public void toResource() {
		final Log log = getLog();
		final LogResource resource = LogConverter.TO_RESOURCE.apply(log);

		assertEquals(resource.getId(), log.getId());
		assertEquals(resource.getMessage(), log.getLogMessage());
		assertEquals(resource.getLevel(), LogLevel.toLevel(log.getLogLevel()).toString());
		assertEquals(resource.getLogTime(), Date.from(log.getLogTime().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getTestItem(), String.valueOf(log.getTestItem().getItemId()));

		final LogResource.BinaryContent binaryContent = resource.getBinaryContent();

		assertEquals(binaryContent.getContentType(), log.getContentType());
		assertEquals(binaryContent.getBinaryDataId(), log.getAttachment());
		assertEquals(binaryContent.getThumbnailId(), log.getAttachmentThumbnail());
	}

	private static Log getLog() {
		Log log = new Log();
		log.setLogLevel(50000);
		log.setLogMessage("message");
		final TestItem testItem = new TestItem();
		testItem.setItemId(1L);
		log.setTestItem(testItem);
		log.setAttachment("attachId");
		log.setContentType("contentType");
		log.setLogTime(LocalDateTime.now());
		log.setId(2L);
		log.setLastModified(LocalDateTime.now());
		log.setAttachmentThumbnail("thumbnailId");
		return log;
	}
}