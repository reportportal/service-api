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

package com.epam.reportportal.base.infrastructure.persistence.binary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.infrastructure.persistence.binary.CreateLogAttachmentService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class CreateLogAttachmentServiceTest {

  @Mock
  private LogRepository logRepository;

  @InjectMocks
  private CreateLogAttachmentService createLogAttachmentService;

  @Test
  void createAttachmentPositive() {
    Log log = getLogWithoutAttachment();
    Attachment attachment = getAttachment();
    when(logRepository.findById(1L)).thenReturn(Optional.of(log));

    createLogAttachmentService.create(attachment, 1L);

    verify(logRepository, times(1)).save(log);

    assertEquals(log.getAttachment().getFileId(), attachment.getFileId());
    assertEquals(log.getAttachment().getThumbnailId(), attachment.getThumbnailId());
    assertEquals(log.getAttachment().getContentType(), attachment.getContentType());
  }

  @Test
  void createAttachmentOnNotExistLog() {
    long logId = 1L;
    when(logRepository.findById(logId)).thenReturn(Optional.empty());

    assertThrows(ReportPortalException.class,
        () -> createLogAttachmentService.create(getAttachment(), logId));
  }

  private Log getLogWithoutAttachment() {
    Log log = new Log();
    log.setId(1L);
    log.setLaunch(new Launch(2L));
    log.setTestItem(new TestItem(3L));
    log.setLogLevel(4000);
    log.setLogMessage("message");
    log.setLogTime(Instant.now());
    return log;
  }

  private Attachment getAttachment() {
    Attachment attachment = new Attachment();
    attachment.setFileId("fileId");
    attachment.setThumbnailId("thumbnailId");
    attachment.setContentType("contentType");
    return attachment;
  }
}
