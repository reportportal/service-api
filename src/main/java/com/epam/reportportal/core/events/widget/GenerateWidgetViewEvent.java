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

package com.epam.reportportal.core.events.widget;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class GenerateWidgetViewEvent {

  private final Long widgetId;
  private final MultiValueMap<String, String> params;

  public GenerateWidgetViewEvent(Long widgetId, MultiValueMap<String, String> params) {
    this.widgetId = widgetId;
    this.params = params;
  }

  public GenerateWidgetViewEvent(Long widgetId) {
    this.widgetId = widgetId;
    this.params = new LinkedMultiValueMap<>();
  }

  public Long getWidgetId() {
    return widgetId;
  }

  public MultiValueMap<String, String> getParams() {
    return params;
  }
}
