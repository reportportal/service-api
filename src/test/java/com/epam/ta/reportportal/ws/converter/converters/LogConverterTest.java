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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogConverterTest {

	private static Log getLog() {
		Log log = new Log();
		log.setLogLevel(50000);
		log.setLogMessage("message");
		final TestItem testItem = new TestItem();
		testItem.setItemId(1L);
		log.setTestItem(testItem);
		Attachment attachment = new Attachment();
		attachment.setFileId("attachId");
		attachment.setContentType("contentType");
		attachment.setThumbnailId("thumbnailId");
		log.setAttachment(attachment);
		log.setLogTime(LocalDateTime.now());
		log.setId(2L);
		log.setUuid("uuid");
		log.setLastModified(LocalDateTime.now());
		return log;
	}

	@Test
	void toResource() {
		final Log log = getLog();
		final LogResource resource = LogConverter.TO_RESOURCE.apply(log);

		assertEquals(resource.getId(), log.getId());
		assertEquals(resource.getUuid(), log.getUuid());
		assertEquals(resource.getMessage(), log.getLogMessage());
		assertEquals(resource.getLevel(), LogLevel.toLevel(log.getLogLevel()).toString());
		assertEquals(resource.getLogTime(), Date.from(log.getLogTime().atZone(ZoneId.of("UTC")).toInstant()));
		assertEquals(resource.getItemId(), log.getTestItem().getItemId());

		final LogResource.BinaryContent binaryContent = resource.getBinaryContent();

		assertEquals(binaryContent.getContentType(), log.getAttachment().getContentType());
		assertEquals(binaryContent.getBinaryDataId(), log.getAttachment().getFileId());
		assertEquals(binaryContent.getThumbnailId(), log.getAttachment().getThumbnailId());
	}
}