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
 * existing log row identified by {@code logId}. The message also carries the project and launch
 * context needed to resolve the integration and persist the downloaded binary. The
 * {@code attachmentExternalId} carries the provider-specific reference (e.g. Mobitru recording id)
 * that the consumer uses to download the actual binary.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAttachmentLoadEvent {

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
}
