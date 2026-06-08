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

package com.epam.reportportal.base.core.item.attribute;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Orchestrator that dispatches a finished {@link TestItem} to all registered
 * {@link TestItemAttributeHandler} beans.
 */
@Component
public class TestItemAttributeHandlerService {

  private final List<TestItemAttributeHandler> attributeHandlers;

  @Autowired
  public TestItemAttributeHandlerService(List<TestItemAttributeHandler> attributeHandlers) {
    this.attributeHandlers = attributeHandlers;
  }

  public void handleTestItemFinish(TestItem testItem) {
    attributeHandlers.forEach(handler -> handler.handleTestItemFinish(testItem));
  }
}
