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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * Task to save log's binary data from MultipartFile for the use with queued RabbitMQ log saving.
 * Statefull, so cannot be a singleton bean.
 *
 * @author Andrei Varabyeu
 */
public class SaveLogBinaryDataTaskAsync implements Supplier<BinaryDataMetaInfo> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaveLogBinaryDataTaskAsync.class);

  @Autowired
  private AttachmentBinaryDataService attachmentBinaryDataService;

  private SaveLogRQ request;

  private MultipartFile file;

  private Long projectId;

  @Override
  public BinaryDataMetaInfo get() {
    Optional<BinaryDataMetaInfo> maybeBinaryDataMetaInfo = attachmentBinaryDataService.saveAttachment(
        AttachmentMetaInfo.builder()
            .withProjectId(projectId)
            .withLaunchUuid(request.getLaunchUuid())
            .withLogUuid(request.getUuid())
            .build(), file);
    return maybeBinaryDataMetaInfo.orElseGet(() -> {
      LOGGER.error("Failed to save log content data into DataStore, projectId {}, itemId {} ",
          projectId, request.getItemUuid());
      throw new ReportPortalException(ErrorType.BINARY_DATA_CANNOT_BE_SAVED);
    });
  }

  public SaveLogBinaryDataTaskAsync withRequest(SaveLogRQ request) {
    Preconditions.checkNotNull(request, "Request shouldn't be null");
    this.request = request;
    return this;
  }

  public SaveLogBinaryDataTaskAsync withFile(MultipartFile file) {
    this.file = file;
    return this;
  }

  public SaveLogBinaryDataTaskAsync withProjectId(Long projectId) {
    Preconditions.checkNotNull(projectId, "Project id should not be null");
    this.projectId = projectId;
    return this;
  }
}
