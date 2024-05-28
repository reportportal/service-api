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

package com.epam.ta.reportportal.core.launch.attribute;

import com.epam.ta.reportportal.entity.launch.Launch;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler for attributes
 *
 * @author Ivan Kustau
 */
@Component
public class LaunchAttributeHandlerService {

  private final List<AttributeHandler> attributeHandlers;

  @Autowired
  public LaunchAttributeHandlerService(List<AttributeHandler> attributeHandlers) {
    this.attributeHandlers = attributeHandlers;
  }

  public void handleLaunchStart(Launch launch) {
    attributeHandlers.forEach(handler -> handler.handleLaunchStart(launch));
  }

  public void handleLaunchUpdate(Launch launch) {
    attributeHandlers.forEach(handler -> handler.handleLaunchUpdate(launch));
  }
}
