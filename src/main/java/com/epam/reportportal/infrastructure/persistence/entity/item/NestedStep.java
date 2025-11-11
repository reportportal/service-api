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

package com.epam.reportportal.infrastructure.persistence.entity.item;

import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.enums.TestItemTypeEnum;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class NestedStep implements Serializable {

  private Long id;

  private String name;

  private String uuid;

  private TestItemTypeEnum type;

  private boolean hasContent;

  private Integer attachmentsCount;

  private StatusEnum status;

  private Instant startTime;

  private Instant endTime;

  private Double duration;

  public NestedStep() {

  }

  public NestedStep(Long id, String name, String uuid, TestItemTypeEnum type, boolean hasContent,
      Integer attachmentsCount,
      StatusEnum status, Instant startTime, Instant endTime, Double duration) {
    this.id = id;
    this.name = name;
    this.uuid = uuid;
    this.type = type;
    this.hasContent = hasContent;
    this.attachmentsCount = attachmentsCount;
    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = duration;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TestItemTypeEnum getType() {
    return type;
  }

  public void setType(TestItemTypeEnum type) {
    this.type = type;
  }

  public boolean isHasContent() {
    return hasContent;
  }

  public void setHasContent(boolean hasContent) {
    this.hasContent = hasContent;
  }

  public Integer getAttachmentsCount() {
    return attachmentsCount;
  }

  public void setAttachmentsCount(Integer attachmentsCount) {
    this.attachmentsCount = attachmentsCount;
  }

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NestedStep that = (NestedStep) o;
    return hasContent == that.hasContent && Objects.equals(id, that.id) && Objects.equals(name,
        that.name) && type == that.type
        && Objects.equals(attachmentsCount, that.attachmentsCount) && status == that.status
        && Objects.equals(startTime,
        that.startTime
    ) && Objects.equals(uuid, that.uuid) && Objects.equals(endTime, that.endTime) && Objects.equals(
        duration,
        that.duration
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, uuid, type, hasContent, attachmentsCount, status, startTime,
        endTime, duration);
  }
}
