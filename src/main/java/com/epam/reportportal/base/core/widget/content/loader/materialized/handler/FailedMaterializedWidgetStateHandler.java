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

import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class FailedMaterializedWidgetStateHandler implements MaterializedWidgetStateHandler {

  private final MaterializedWidgetStateHandler refreshWidgetStateHandler;

  @Autowired
  public FailedMaterializedWidgetStateHandler(
      @Qualifier("createdMaterializedWidgetStateHandler") MaterializedWidgetStateHandler refreshWidgetStateHandler) {
    this.refreshWidgetStateHandler = refreshWidgetStateHandler;
  }

  @Override
  public Map<String, Object> handleWidgetState(Widget widget,
      MultiValueMap<String, String> params) {
    if (BooleanUtils.toBoolean(params.getFirst(REFRESH))) {
      return refreshWidgetStateHandler.handleWidgetState(widget, params);
    }
    return Collections.emptyMap();
  }
}
