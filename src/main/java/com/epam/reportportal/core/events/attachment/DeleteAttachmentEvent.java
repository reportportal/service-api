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

package com.epam.reportportal.core.events.attachment;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Event published when attachments are deleted.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Getter
@Setter
public class DeleteAttachmentEvent extends AbstractEvent<Void> {

  @JsonProperty(value = "ids")
  private List<Long> ids;

  @JsonProperty(value = "paths")
  private List<String> paths;

  public DeleteAttachmentEvent() {
    super();
    this.ids = Lists.newArrayList();
    this.paths = Lists.newArrayList();
  }
}
