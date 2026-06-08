/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.events.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Event published to request asynchronous fetching of an external attachment and attaching it to an
 * existing log row identified by {@code logId}. The message also carries the plugin command,
 * project and launch context needed to resolve the integration and persist the downloaded binary.
 * The {@code attachmentExternalId} carries the provider-specific reference that the consumer uses
 * to download the actual binary. {@code attachmentAttributeKey} carries the source attribute key
 * when the provider needs it to choose the download endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAttachmentLoadEvent {

  @JsonProperty("pluginId")
  private String pluginId;

  @JsonProperty("pluginCommandName")
  private String pluginCommandName;

  @JsonProperty("logId")
  private Long logId;

  @JsonProperty("projectId")
  private Long projectId;

  @JsonProperty("launchId")
  private Long launchId;

  @JsonProperty("testItemId")
  private Long testItemId;

  @JsonProperty("attachmentExternalId")
  private String attachmentExternalId;

  @JsonProperty("attachmentAttributeKey")
  private String attachmentAttributeKey;

  public ExternalAttachmentLoadEvent(Long logId, Long projectId, Long launchId, Long testItemId,
      String attachmentExternalId) {
    this(logId, projectId, launchId, testItemId, attachmentExternalId, null);
  }

  public ExternalAttachmentLoadEvent(Long logId, Long projectId, Long launchId, Long testItemId,
      String attachmentExternalId, String attachmentAttributeKey) {
    this(null, null, logId, projectId, launchId, testItemId, attachmentExternalId,
        attachmentAttributeKey);
  }
}
