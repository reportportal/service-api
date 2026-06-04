/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.extension.bugtracking;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ImageFormat;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import org.apache.tika.mime.MediaType;

/**
 * Internal representation of a bug tracking ticket to be posted to an external Bug Tracking System.
 *
 * @author Andrei Varabyeu
 */
public class InternalTicket {

  private String summary;

  private String comments;

  private Multimap<String, String> fields;

  private List<LogEntry> logs;

  /**
   * Item --> Item URL map
   */
  private Map<Long, String> backLinks;

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String value) {
    this.comments = value;
  }

  public Multimap<String, String> getFields() {
    return fields;
  }

  public void setFields(Multimap<String, String> fields) {
    this.fields = fields;
  }

  public List<LogEntry> getLogs() {
    return logs;
  }

  public void setLogs(List<LogEntry> logs) {
    this.logs = logs;
  }

  public Map<Long, String> getBackLinks() {
    return backLinks;
  }

  public void setBackLinks(Map<Long, String> backLinks) {
    this.backLinks = backLinks;
  }

  public static class LogEntry {

    private Long logId;
    private String message;
    private boolean isIncludeLogs;
    private boolean hasAttachment;
    private String fileId;
    private String decodedFileName;
    private String contentType;
    private boolean isImage;

    public LogEntry(Long logId, String message, boolean includeLogs) {
      this.logId = logId;
      this.message = message;
      this.isIncludeLogs = includeLogs;
    }

    public LogEntry(Long logId, String message, boolean isIncludeLogs, boolean hasAttachment, String fileId,
        String decodedFileName,
        String contentType) {
      this.logId = logId;
      this.message = message;
      this.isIncludeLogs = isIncludeLogs;
      this.hasAttachment = hasAttachment;
      this.fileId = fileId;
      this.decodedFileName = decodedFileName;
      this.contentType = contentType;
      this.isImage = ImageFormat.fromValue(MediaType.parse(contentType).getSubtype()).isPresent();
    }

    public Long getLogId() {
      return logId;
    }

    public String getMessage() {
      return message;
    }

    public boolean isIncludeLogs() {
      return isIncludeLogs;
    }

    public boolean isHasAttachment() {
      return hasAttachment;
    }

    public String getFileId() {
      return fileId;
    }

    public String getDecodedFileName() {
      return decodedFileName;
    }

    public String getContentType() {
      return contentType;
    }

    public boolean isImage() {
      return isImage;
    }
  }
}
