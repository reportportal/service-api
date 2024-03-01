/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.widget.content.remover;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.core.widget.content.materialized.state.WidgetStateResolver;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetState;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.google.common.collect.Lists;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class DelegatingStateContentRemover implements WidgetContentRemover {

  private final WidgetStateResolver widgetStateResolver;
  private final Map<WidgetState, WidgetContentRemover> widgetContentRemoverMapping;

  @Autowired
  public DelegatingStateContentRemover(WidgetStateResolver widgetStateResolver,
      Map<WidgetState, WidgetContentRemover> widgetContentRemoverMapping) {
    this.widgetStateResolver = widgetStateResolver;
    this.widgetContentRemoverMapping = widgetContentRemoverMapping;
  }

  @Override
  public void removeContent(Widget widget) {
    if (supports(widget)) {
      final WidgetState state = widgetStateResolver.resolve(widget.getWidgetOptions());
      ofNullable(widgetContentRemoverMapping.get(state)).ifPresent(
          remover -> remover.removeContent(widget));
    }
  }

  private boolean supports(Widget widget) {
    return Lists.newArrayList(WidgetType.COMPONENT_HEALTH_CHECK_TABLE.getType(),
            WidgetType.CUMULATIVE.getType())
        .contains(widget.getWidgetType());
  }

}
