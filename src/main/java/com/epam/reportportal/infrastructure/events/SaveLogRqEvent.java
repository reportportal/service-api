/*
 * Copyright 2023 EPAM Systems
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

import com.epam.reportportal.reporting.SaveLogRQ;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
public class SaveLogRqEvent extends ApplicationEvent {

  private final String projectName;
  private final SaveLogRQ saveLogRQ;
  private final MultipartFile file;

  public SaveLogRqEvent(Object source, String projectName,
      SaveLogRQ saveLogRQ, MultipartFile file) {
    super(source);
    this.projectName = projectName;
    this.saveLogRQ = saveLogRQ;
    this.file = file;
  }
}
