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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.ta.reportportal.dao.LogTypeRepository;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.model.log.LogResource;
import com.epam.ta.reportportal.service.LogTypeResolver;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LogConverterTest {

  @Mock
  private LogTypeRepository logTypeRepository;

  private LogConverter logConverter;

  @BeforeEach
  void setUp() {
    logConverter = new LogConverter((new LogTypeResolver(logTypeRepository)));
  }

  private static LogFull getLogFull() {
    LogFull logFull = new LogFull();
    logFull.setLogLevel(50000);
    logFull.setLogMessage("message");
    logFull.setProjectId(1L);
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

  private static LogFull getLogFullWithCustomLogType(Long projectId, int customLogLevel) {
    LogFull logFull = new LogFull();
    logFull.setLogLevel(customLogLevel);
    logFull.setLogMessage("custom log message");
    logFull.setProjectId(projectId);
    final TestItem testItem = new TestItem();
    testItem.setItemId(2L);
    logFull.setTestItem(testItem);
    logFull.setLogTime(Instant.now());
    logFull.setId(3L);
    logFull.setUuid("custom-uuid");
    logFull.setLastModified(Instant.now());
    return logFull;
  }

  @Test
  void toResource() {
    // Given
    final LogFull logFull = getLogFull();

    // When
    final LogResource resource = logConverter.toResource(logFull);

    // Then
    assertEquals(resource.getId(), logFull.getId());
    assertEquals(resource.getUuid(), logFull.getUuid());
    assertEquals(resource.getMessage(), logFull.getLogMessage());
    assertEquals("FATAL", resource.getLevel());
    assertEquals(resource.getLogTime().truncatedTo(ChronoUnit.SECONDS),
        Instant.now().truncatedTo(ChronoUnit.SECONDS));
    assertEquals(resource.getItemId(), logFull.getTestItem().getItemId());

    final LogResource.BinaryContent binaryContent = resource.getBinaryContent();

    assertEquals(binaryContent.getContentType(), logFull.getAttachment().getContentType());
    assertEquals(binaryContent.getBinaryDataId(), String.valueOf(logFull.getAttachment().getId()));
    assertEquals(binaryContent.getThumbnailId(), logFull.getAttachment().getThumbnailId());
  }

  @Test
  void toResourceWithCustomLogType() {
    // Given
    Long projectId = 1L;
    int customLogLevel = 8500;
    String customLogTypeName = "CUSTOM1";

    when(logTypeRepository.findNameByProjectIdAndLevel(projectId, customLogLevel))
        .thenReturn(customLogTypeName);

    final LogFull logFull = getLogFullWithCustomLogType(projectId, customLogLevel);

    // When
    final LogResource resource = logConverter.toResource(logFull);

    // Then
    assertEquals(resource.getId(), logFull.getId());
    assertEquals(resource.getUuid(), logFull.getUuid());
    assertEquals(resource.getMessage(), logFull.getLogMessage());
    assertEquals("CUSTOM1", resource.getLevel());
    assertEquals(resource.getItemId(), logFull.getTestItem().getItemId());
  }

  @Test
  void toResourceFallbackToEnumWhenProjectIdIsNull() {
    // Given
    final LogFull logFull = getLogFull();
    logFull.setProjectId(null);
    logFull.setLogLevel(40000);

    // When
    final LogResource resource = logConverter.toResource(logFull);

    // Then
    assertEquals("ERROR", resource.getLevel());
  }

  @Test
  void toResourceWithStandardLogLevel() {
    // Given
    final LogFull logFull = getLogFull();
    logFull.setLogLevel(40000);

    // When
    final LogResource resource = logConverter.toResource(logFull);

    // Then
    assertEquals("ERROR", resource.getLevel());
  }

  @Test
  void toResourceHandlesUnknownLevelFallback() {
    // Given
    when(logTypeRepository.findNameByProjectIdAndLevel(anyLong(), anyInt()))
        .thenReturn(null);

    final LogFull logFull = getLogFull();
    logFull.setLogLevel(99999);

    // When
    final LogResource resource = logConverter.toResource(logFull);

    // Then
    assertEquals("UNKNOWN", resource.getLevel());
  }
}
