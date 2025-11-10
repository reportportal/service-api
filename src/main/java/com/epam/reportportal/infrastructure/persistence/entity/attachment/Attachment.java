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

import com.epam.reportportal.infrastructure.persistence.dao.converters.JpaInstantConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Entity
@Table(name = "attachment")
public class Attachment implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "file_id")
  private String fileId;

  @Column(name = "thumbnail_id")
  private String thumbnailId;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "file_size")
  private long fileSize;

  @Column(name = "creation_date")
  @Convert(converter = JpaInstantConverter.class)
  private Instant creationDate;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "launch_id")
  private Long launchId;

  @Column(name = "item_id")
  private Long itemId;

  public Attachment() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getThumbnailId() {
    return thumbnailId;
  }

  public void setThumbnailId(String thumbnailId) {
    this.thumbnailId = thumbnailId;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public Instant getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Instant creationDate) {
    this.creationDate = creationDate;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getLaunchId() {
    return launchId;
  }

  public void setLaunchId(Long launchId) {
    this.launchId = launchId;
  }

  public Long getItemId() {
    return itemId;
  }

  public void setItemId(Long itemId) {
    this.itemId = itemId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Attachment that = (Attachment) o;
    return Objects.equals(fileId, that.fileId) && Objects.equals(thumbnailId, that.thumbnailId)
        && Objects.equals(contentType,
        that.contentType
    ) && Objects.equals(fileSize, that.fileSize) && Objects.equals(creationDate, that.creationDate)
        && Objects.equals(fileName, that.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileId, thumbnailId, contentType, fileSize, creationDate, fileName);
  }
}
