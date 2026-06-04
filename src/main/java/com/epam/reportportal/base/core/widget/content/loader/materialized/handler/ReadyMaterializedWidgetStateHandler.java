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

package com.epam.reportportal.base.core.widget.content.loader.materialized.handler;

import static com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers.formattedSupplier;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.core.widget.content.loader.materialized.MaterializedWidgetContentLoader;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.WidgetType;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * Handles content loading for widgets whose materialized view is in a READY state.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ReadyMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

  private final MaterializedWidgetStateHandler refreshWidgetStateHandler;
  private final Map<WidgetType, MaterializedWidgetContentLoader> materializedWidgetContentLoaderMapping;

  public ReadyMaterializedWidgetStateHandler(
      @Qualifier("createdMaterializedWidgetStateHandler") MaterializedWidgetStateHandler refreshWidgetStateHandler,
      @Qualifier("materializedWidgetContentLoaderMapping")
      Map<WidgetType, MaterializedWidgetContentLoader> materializedWidgetContentLoaderMapping) {
    this.refreshWidgetStateHandler = refreshWidgetStateHandler;
    this.materializedWidgetContentLoaderMapping = materializedWidgetContentLoaderMapping;
  }

  @Override
  public Map<String, Object> handleWidgetState(Widget widget,
      MultiValueMap<String, String> params) {

    if (BooleanUtils.toBoolean(params.getFirst(REFRESH))) {
      return refreshWidgetStateHandler.handleWidgetState(widget, params);
    }

    WidgetType widgetType = WidgetType.findByName(widget.getWidgetType())
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
            formattedSupplier("Unsupported widget type '{}'", widget.getWidgetType())
        ));

    return ofNullable(materializedWidgetContentLoaderMapping.get(widgetType)).map(
            loader -> loader.loadContent(widget, params))
        .orElseGet(Collections::emptyMap);
  }
}
