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

package com.epam.reportportal.base.core.widget.content.updater;

import com.epam.reportportal.base.core.widget.content.updater.validator.WidgetValidator;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Post-processor that updates Component Health Check widget state after content changes.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class ComponentHealthCheckPostProcessor implements WidgetPostProcessor {

  private final WidgetValidator componentHealthCheckValidator;

  @Autowired
  public ComponentHealthCheckPostProcessor(WidgetValidator componentHealthCheckValidator) {
    this.componentHealthCheckValidator = componentHealthCheckValidator;
  }

  @Override
  public boolean supports(Widget widget) {
    return WidgetType.COMPONENT_HEALTH_CHECK.getType().equalsIgnoreCase(widget.getWidgetType());
  }

  @Override
  public void postProcess(Widget widget) {
    if (supports(widget)) {
      componentHealthCheckValidator.validate(widget);
    }
  }
}
