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

package com.epam.reportportal.base.core.widget.content.materialized.generator;

import static com.epam.reportportal.base.core.widget.content.loader.materialized.handler.MaterializedWidgetStateHandler.VIEW_NAME;

import com.epam.reportportal.base.core.widget.util.WidgetOptionUtil;
import com.epam.reportportal.base.infrastructure.persistence.entity.widget.Widget;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Generates the materialized view name for a given widget.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class MaterializedViewNameGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(MaterializedViewNameGenerator.class);

  private static final String VIEW_PREFIX = "widget";
  private static final String NAME_SEPARATOR = "_";

  public String generate(Widget widget) {
    return String.join(NAME_SEPARATOR, VIEW_PREFIX, String.valueOf(widget.getProject().getId()),
        String.valueOf(widget.getId()));
  }

  /**
   * Resolves the materialized view name of a widget that has one. The name stored in the widget
   * options is never used as is: it is accepted only when it equals the name generated from the
   * widget's own project and id, and the generated value is returned.
   *
   * @param widget widget with materialized content
   * @return generated view name, or empty when the widget has no stored view name or the stored
   *     name does not belong to this widget
   */
  public Optional<String> resolveStored(Widget widget) {
    String stored = WidgetOptionUtil.getValueByKey(VIEW_NAME, widget.getWidgetOptions());
    if (stored == null) {
      return Optional.empty();
    }
    String generated = generate(widget);
    if (!generated.equals(stored)) {
      LOGGER.warn("Widget {}: stored materialized view name does not match the expected one",
          widget.getId());
      return Optional.empty();
    }
    return Optional.of(generated);
  }
}
