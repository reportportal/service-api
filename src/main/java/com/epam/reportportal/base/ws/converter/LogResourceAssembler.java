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

package com.epam.reportportal.base.ws.converter;

import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogFull;
import com.epam.reportportal.base.model.log.LogResource;
import com.epam.reportportal.base.ws.converter.converters.LogConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Assembler for LogResources
 *
 * @author Andrei Varabyeu
 */
@Service
@RequiredArgsConstructor
public class LogResourceAssembler extends PagedResourcesAssembler<LogFull, LogResource> {

  private final LogConverter logConverter;

  @Override
  public LogResource toResource(LogFull log) {
    return logConverter.toResource(log);
  }
}
