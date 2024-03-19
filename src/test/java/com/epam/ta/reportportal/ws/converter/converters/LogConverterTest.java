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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.model.log.LogResource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogConverterTest {

  private static LogFull getLogFull() {
    LogFull logFull = new LogFull();
    logFull.setLogLevel(50000);
    logFull.setLogMessage("message");
    final TestItem testItem = new TestItem();
    testItem.setItemId(1L);
    logFull.setTestItem(testItem);
    Attachment attachment = new Attachment();
    attachment.setId(1L);
    attachment.setFileId("attachId");
    attachment.setContentType("contentType");
    attachment.setThumbnailId("thumbnailId");
    logFull.setAttachment(attachment);
    logFull.setLogTime(Instant.now());
    logFull.setId(2L);
    logFull.setUuid("uuid");
    logFull.setLastModified(Instant.now());
    return logFull;
  }

  @Test
  void toResource() {
    final LogFull logFull = getLogFull();
    final LogResource resource = LogConverter.TO_RESOURCE.apply(logFull);

    assertEquals(resource.getId(), logFull.getId());
    assertEquals(resource.getUuid(), logFull.getUuid());
    assertEquals(resource.getMessage(), logFull.getLogMessage());
    assertEquals(resource.getLevel(), LogLevel.toLevel(logFull.getLogLevel()).toString());
    assertEquals(resource.getLogTime().truncatedTo(ChronoUnit.SECONDS),
        Instant.now().truncatedTo(ChronoUnit.SECONDS));
    assertEquals(resource.getItemId(), logFull.getTestItem().getItemId());

    final LogResource.BinaryContent binaryContent = resource.getBinaryContent();

    assertEquals(binaryContent.getContentType(), logFull.getAttachment().getContentType());
    assertEquals(binaryContent.getBinaryDataId(), String.valueOf(logFull.getAttachment().getId()));
    assertEquals(binaryContent.getThumbnailId(), logFull.getAttachment().getThumbnailId());
  }
}
