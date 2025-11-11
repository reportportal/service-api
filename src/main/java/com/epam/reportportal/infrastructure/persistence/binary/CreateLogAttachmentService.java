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

package com.epam.reportportal.infrastructure.persistence.binary;

import com.epam.reportportal.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class CreateLogAttachmentService {

  private final LogRepository logRepository;

  @Autowired
  public CreateLogAttachmentService(LogRepository logRepository) {
    this.logRepository = logRepository;
  }

  public void create(Attachment attachment, Long logId) {
    Log log = logRepository.findById(logId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LOG_NOT_FOUND, logId));
    log.setAttachment(attachment);
    logRepository.save(log);
  }
}
