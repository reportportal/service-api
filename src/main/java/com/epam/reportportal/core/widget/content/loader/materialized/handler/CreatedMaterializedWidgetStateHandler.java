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

package com.epam.reportportal.core.widget.content.loader.materialized.handler;

import static com.epam.reportportal.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Collections.emptyMap;

import com.epam.reportportal.core.events.widget.GenerateWidgetViewEvent;
import com.epam.reportportal.infrastructure.persistence.dao.WidgetRepository;
import com.epam.reportportal.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.infrastructure.persistence.entity.widget.WidgetState;
import com.epam.reportportal.ws.converter.builders.WidgetBuilder;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CreatedMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

  private final WidgetRepository widgetRepository;
  protected ApplicationEventPublisher eventPublisher;

  public CreatedMaterializedWidgetStateHandler(WidgetRepository widgetRepository,
      @Qualifier("webApplicationContext") ApplicationEventPublisher eventPublisher) {
    this.widgetRepository = widgetRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public Map<String, Object> handleWidgetState(Widget widget,
      MultiValueMap<String, String> params) {
    widgetRepository.save(
        new WidgetBuilder(widget).addOption(STATE, WidgetState.RENDERING.getValue()).get());
    eventPublisher.publishEvent(new GenerateWidgetViewEvent(widget.getId(), params));
    return emptyMap();
  }

}
