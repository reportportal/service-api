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

package com.epam.reportportal.infrastructure.events;

import com.epam.reportportal.core.events.domain.AbstractEvent;
import com.epam.reportportal.reporting.SaveLogRQ;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * System event published when a log save request is received.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
@NoArgsConstructor
public class SaveLogRqEvent extends AbstractEvent<Void> {

  private String projectName;
  private SaveLogRQ saveLogRQ;
  private MultipartFile file;

  /**
   * Constructs a SaveLogRqEvent.
   *
   * @param projectName The name of the project
   * @param saveLogRQ   The save log request
   * @param file        The multipart file (if any)
   */
  public SaveLogRqEvent(String projectName, SaveLogRQ saveLogRQ, MultipartFile file) {
    super();
    this.projectName = projectName;
    this.saveLogRQ = saveLogRQ;
    this.file = file;
  }

  @Override
  public boolean shouldPublishToRabbitMQ() {
    return false;
  }
}
