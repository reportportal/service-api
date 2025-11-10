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

package com.epam.reportportal.infrastructure.persistence.entity.attachment;

import com.google.common.base.Preconditions;
import java.time.Instant;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class AttachmentMetaInfo {

  private Long projectId;

  private Long launchId;

  private Long itemId;

  private Long logId;

  private String launchUuid;

  private String logUuid;

  private String fileName;

  private Instant creationDate;

  public AttachmentMetaInfo(Long projectId, Long launchId, Long itemId, Long logId,
      String launchUuid, String logUuid,
      Instant creationDate, String fileName) {
    this.projectId = projectId;
    this.launchId = launchId;
    this.itemId = itemId;
    this.logId = logId;
    this.launchUuid = launchUuid;
    this.logUuid = logUuid;
    this.creationDate = creationDate;
    this.fileName = fileName;
  }

  public static AttachmentMetaInfoBuilder builder() {
    return new AttachmentMetaInfoBuilder();
  }

  public Long getProjectId() {
    return projectId;
  }

  public Long getLaunchId() {
    return launchId;
  }

  public Long getItemId() {
    return itemId;
  }

  public Long getLogId() {
    return logId;
  }

  public String getLaunchUuid() {
    return launchUuid;
  }

  public String getLogUuid() {
    return logUuid;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public static class AttachmentMetaInfoBuilder {

    private Long projectId;

    private Long launchId;

    private Long itemId;

    private Long logId;

    private String launchUuid;

    private String logUuid;

    private Instant creationDate;

    private String fileName;

    private AttachmentMetaInfoBuilder() {
    }

    public AttachmentMetaInfoBuilder withProjectId(Long projectId) {
      Preconditions.checkNotNull(projectId);
      this.projectId = projectId;
      return this;
    }

    public AttachmentMetaInfoBuilder withLaunchId(Long launchId) {
      this.launchId = launchId;
      return this;
    }

    public AttachmentMetaInfoBuilder withItemId(Long itemId) {
      this.itemId = itemId;
      return this;
    }

    public AttachmentMetaInfoBuilder withLogId(Long logId) {
      this.logId = logId;
      return this;
    }

    public AttachmentMetaInfoBuilder withLaunchUuid(String launchUuid) {
      this.launchUuid = launchUuid;
      return this;
    }

    public AttachmentMetaInfoBuilder withLogUuid(String logUuid) {
      this.logUuid = logUuid;
      return this;
    }

    public AttachmentMetaInfoBuilder withCreationDate(Instant creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    public AttachmentMetaInfoBuilder withFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public AttachmentMetaInfo build() {
      return new AttachmentMetaInfo(this.projectId, this.launchId, this.itemId, this.logId,
          this.launchUuid, this.logUuid, this.creationDate, this.fileName);
    }
  }
}
