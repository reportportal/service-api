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

package com.epam.reportportal.base.core.widget.content;

import static com.epam.reportportal.base.core.widget.content.updater.MaterializedWidgetStateUpdater.STATE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler;
import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetState;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

/**
 * Default implementation of {@link MaterializedLoadContentStrategy} that delegates to a state-specific handler.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class MaterializedLoadContentStrategyImpl implements MaterializedLoadContentStrategy {

  private final Map<WidgetState, MaterializedWidgetStateHandler> widgetStateHandlerMapping;

  @Autowired
  public MaterializedLoadContentStrategyImpl(
      @Qualifier("widgetStateHandlerMapping") Map<WidgetState, MaterializedWidgetStateHandler> widgetStateHandlerMapping) {
    this.widgetStateHandlerMapping = widgetStateHandlerMapping;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Map<String, Object> loadContent(Widget widget, MultiValueMap<String, String> params) {

    WidgetState widgetState = ofNullable(WidgetOptionUtil.getValueByKey(STATE,
        widget.getWidgetOptions()
    )).flatMap(WidgetState::findByName)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_LOAD_WIDGET_CONTENT,
            "Widget state not provided"));

    return widgetStateHandlerMapping.get(widgetState).handleWidgetState(widget, params);
  }
}
